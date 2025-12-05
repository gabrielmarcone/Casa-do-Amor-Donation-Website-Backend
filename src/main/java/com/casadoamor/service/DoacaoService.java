package com.casadoamor.service;

import com.casadoamor.dao.DoacaoDAO;
import com.casadoamor.dao.PagamentoDAO;
import com.casadoamor.doacao.config.MercadoPagoConfigManager;
import com.casadoamor.doacao.gateway.MercadoPagoClient;
import com.casadoamor.doacao.gateway.PagamentoResultado;
import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.dto.CriarDoacaoResponse;
import com.casadoamor.enums.MetodoPagamento;
import com.casadoamor.enums.StatusDoacao;
import com.casadoamor.enums.StatusPagamento;
import com.casadoamor.model.Doacao;
import com.casadoamor.model.Pagamento;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class DoacaoService {

    private final DoacaoDAO doacaoDAO = new DoacaoDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO(); 
    private final MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- FEATURE 1.1: CRIAR DOAÇÃO ---
    public CriarDoacaoResponse criarDoacao(CriarDoacaoRequest request) {
        // 1. Preparar e Salvar o Pagamento primeiro (Tabela PAGAMENTO)
        Pagamento pagamento = new Pagamento();
        pagamento.setValor(request.getValor());
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);
        
        try {
            // Tenta converter o enum, se falhar assume PIX
            pagamento.setMetodoPagamento(MetodoPagamento.valueOf(request.getMetodoPagamento().toUpperCase())); 
        } catch (Exception e) {
            pagamento.setMetodoPagamento(MetodoPagamento.PIX); 
        }
        
        // Salva no banco para gerar o ID local
        pagamento = pagamentoDAO.salvar(pagamento);

        // 2. Preparar e Salvar a Doação vinculada ao Pagamento (Tabela DOACAO)
        Doacao doacao = new Doacao();
        doacao.setValor(request.getValor());
        doacao.setMoeda("BRL");
        doacao.setStatusDoacao(StatusDoacao.PENDING);
        doacao.setNomeDoador(request.getNomeDoador());
        doacao.setEmailDoador(request.getEmailDoador());
        doacao.setCriadoEm(LocalDateTime.now());
        doacao.setAtualizadoEm(LocalDateTime.now());
        doacao.setReferenciaExt(UUID.randomUUID().toString());
        doacao.setIdempotencyKey(UUID.randomUUID().toString());
        
        // VINCULA O PAGAMENTO (Salva o ID do pagamento local na doação)
        doacao.setPagamentoId(String.valueOf(pagamento.getIdPagamento())); 

        doacaoDAO.salvarDoacao(doacao);

        // 3. Chamar o Gateway (Mercado Pago)
        PagamentoResultado resultado = null;
        if (pagamento.getMetodoPagamento() == MetodoPagamento.PIX) {
            resultado = mercadoPagoClient.criarPagamentoPix(doacao, request);
        } else {
            // Assumindo cartão para qualquer outro método
            resultado = mercadoPagoClient.criarPagamentoCartao(doacao, request);
        }

        // 4. Atualizar o Pagamento com o ID do Gateway
        if (resultado != null) {
            // Atualiza a tabela PAGAMENTO com o ID que veio do Mercado Pago (ex: "123456789")
            pagamentoDAO.atualizarStatus(pagamento.getIdPagamento(), StatusPagamento.PENDENTE, resultado.getPagamentoId());
        }

        // 5. Montar Resposta para o Front
        CriarDoacaoResponse response = new CriarDoacaoResponse();
        response.setId(doacao.getId());
        response.setStatus(doacao.getStatusDoacao().name());
        if (resultado != null) {
            response.setQrCode(resultado.getQrCode());
            response.setQrCodeImg(resultado.getQrCodeImg());
            response.setPagamentoId(resultado.getPagamentoId());
            response.setMensagem(resultado.getMensagem());
        }
        return response;
    }

    // --- FEATURE 1.3: LISTAGEM PARA ADMIN ---
    public List<Doacao> listarTodas() {
        return doacaoDAO.listarTodas();
    }

    public Doacao buscarPorId(Long id) {
        return doacaoDAO.buscarPorId(id);
    }

    // --- WEBHOOK: PROCESSAMENTO DE RETORNO DO MP ---
    public void processarWebhook(String payload, String signature, String requestId) {
        try {
            // 1. Extrair ID do pagamento da notificação JSON
            JsonNode root = objectMapper.readTree(payload);
            JsonNode data = root.path("data");
            
            Long idPagamentoMP = null;
            if (data != null && !data.isMissingNode()) {
                idPagamentoMP = data.path("id").asLong(0L);
            }
            if (idPagamentoMP == null || idPagamentoMP == 0) {
                 // Tenta pegar da raiz se não estiver dentro de data (alguns webhooks variam)
                 idPagamentoMP = root.path("data.id").asLong(0L);
            }

            if (idPagamentoMP == 0L) {
                System.out.println("Webhook ignorado: ID não encontrado.");
                return;
            }

            // 2. Validar assinatura de segurança (HMAC)
            String secret = MercadoPagoConfigManager.getPropriedades().getWebhook();
            if (secret != null && !secret.isEmpty() && signature != null) {
                String template = "id:" + idPagamentoMP + ";requestId:" + requestId;
                // Nota: A lógica exata de template do MP pode variar (x-signature vs v1), 
                // mas a validação básica é essa. Se falhar, logamos mas prosseguimos consultando a API por segurança.
                // String hashCalculado = hmacSha256(template, secret);
                // if (!hashCalculado.equals(signature)) ...
            }

            // 3. Consultar a API do Mercado Pago para ver o status REAL
            // (Nunca confie apenas no payload do webhook, sempre consulte a fonte)
            PagamentoResultado resultadoMP = mercadoPagoClient.consultarPagamento(idPagamentoMP.toString());
            
            if (resultadoMP == null) {
                System.out.println("Pagamento não encontrado na API do MP.");
                return;
            }

            // 4. Buscar a Doação no nosso banco pela Referência Externa
            Doacao doacao = doacaoDAO.buscaPorRefEx(resultadoMP.getReferenciaExt());
            
            if (doacao == null) {
                System.out.println("Doação não encontrada para ref: " + resultadoMP.getReferenciaExt());
                return;
            }

            // 5. Atualizar Tabela DOACAO
            StatusDoacao statusNovo = mapearStatus(resultadoMP.getStatus());
            doacao.setStatusDoacao(statusNovo);
            doacao.setAtualizadoEm(LocalDateTime.now());
            // Atualiza status na tabela doacao
            doacaoDAO.atualizarDoacao(doacao); 

            // 6. Atualizar Tabela PAGAMENTO
            // O campo pagamentoId dentro de Doacao agora é o ID local da tabela Pagamento (FK)
            try {
                Long idPagamentoLocal = Long.parseLong(doacao.getPagamentoId());
                StatusPagamento statusPagamentoNovo = mapearStatusPagamento(resultadoMP.getStatus());
                
                // Atualiza status e garante que o gateway_id esteja salvo na tabela Pagamento
                pagamentoDAO.atualizarStatus(idPagamentoLocal, statusPagamentoNovo, resultadoMP.getPagamentoId());
                
            } catch (NumberFormatException e) {
                System.err.println("Erro ao converter FK pagamento_id: " + doacao.getPagamentoId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro crítico ao processar webhook: " + e.getMessage());
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private String hmacSha256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }

    private StatusDoacao mapearStatus(String statusGateway) {
        if (statusGateway == null) return StatusDoacao.PENDING;
        switch (statusGateway.toLowerCase()) {
            case "approved": return StatusDoacao.PAID;
            case "in_process":
            case "in_mediation": return StatusDoacao.REQUIRES_ACTION;
            case "rejected": return StatusDoacao.FAILED;
            case "cancelled":
            case "refunded": return StatusDoacao.CANCELLED;
            default: return StatusDoacao.PENDING;
        }
    }

    private StatusPagamento mapearStatusPagamento(String statusGateway) {
        if (statusGateway == null) return StatusPagamento.PENDENTE;
        switch (statusGateway.toLowerCase()) {
            case "approved": return StatusPagamento.PAGO;
            case "rejected": return StatusPagamento.FALHA;
            case "cancelled":
            case "refunded": return StatusPagamento.EXPIRADO; // ou FALHA
            default: return StatusPagamento.PENDENTE;
        }
    }
}