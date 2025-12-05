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
import java.math.BigDecimal;
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

    // --- FEATURE 1.1: CRIAR DOAÇÃO (CHECKOUT) ---
    public CriarDoacaoResponse criarDoacao(CriarDoacaoRequest request) {
        
        // Validação RN-002: Valor mínimo
        if (request.getValor() == null || request.getValor().compareTo(new BigDecimal("10.00")) < 0) {
            throw new IllegalArgumentException("O valor mínimo para doação é de R$ 10,00.");
        }

        // 1. Preparar e Salvar o Pagamento primeiro (Tabela PAGAMENTO)
        Pagamento pagamento = new Pagamento();
        pagamento.setValor(request.getValor());
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);

        try {
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

        // VINCULA O PAGAMENTO
        doacao.setPagamentoId(String.valueOf(pagamento.getIdPagamento()));

        doacaoDAO.salvarDoacao(doacao);

        // 3. Chamar o Gateway (Mercado Pago)
        PagamentoResultado resultado = null;
        if (pagamento.getMetodoPagamento() == MetodoPagamento.PIX) {
            resultado = mercadoPagoClient.criarPagamentoPix(doacao, request);
        } else {
            // Assumindo cartão
            resultado = mercadoPagoClient.criarPagamentoCartao(doacao, request);
        }

        // 4. Atualizar o Pagamento com o ID do Gateway
        if (resultado != null) {
            pagamentoDAO.atualizarStatus(pagamento.getIdPagamento(), StatusPagamento.PENDENTE, resultado.getPagamentoId());
        }

        // 5. Montar Resposta
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
            JsonNode root = objectMapper.readTree(payload);
            JsonNode data = root.path("data");

            Long idPagamentoMP = null;
            if (data != null && !data.isMissingNode()) {
                idPagamentoMP = data.path("id").asLong(0L);
            }
            if (idPagamentoMP == null || idPagamentoMP == 0) {
                idPagamentoMP = root.path("data.id").asLong(0L);
            }

            if (idPagamentoMP == 0L) {
                System.out.println("Webhook ignorado: ID não encontrado.");
                return;
            }

            // Consultar a API do Mercado Pago para ver o status REAL
            PagamentoResultado resultadoMP = mercadoPagoClient.consultarPagamento(idPagamentoMP.toString());

            if (resultadoMP == null) {
                System.out.println("Pagamento não encontrado na API do MP.");
                return;
            }

            // Buscar a Doação no nosso banco pela Referência Externa
            Doacao doacao = doacaoDAO.buscaPorRefEx(resultadoMP.getReferenciaExt());

            // --- TRATAMENTO DE RECORRÊNCIA (ASSINATURA) ---
            if (doacao == null) {
                String ref = resultadoMP.getReferenciaExt();
                // Verifica se é uma referência de assinatura (Ex: "ASSIN-55")
                if (ref != null && ref.startsWith("ASSIN-")) {
                    System.out.println("Recorrência detectada! Criando nova doação para: " + ref);

                    // A. Criar novo Pagamento no Banco
                    Pagamento novoPagamento = new Pagamento();
                    novoPagamento.setValor(BigDecimal.ZERO); // Idealmente pegaria o valor do JSON do MP
                    novoPagamento.setMetodoPagamento(MetodoPagamento.CARTAO_CREDITO);
                    novoPagamento.setDataPagamento(LocalDateTime.now());
                    novoPagamento.setStatusPagamento(mapearStatusPagamento(resultadoMP.getStatus()));
                    novoPagamento.setGatewayId(resultadoMP.getPagamentoId());

                    novoPagamento = pagamentoDAO.salvar(novoPagamento);

                    // B. Criar nova Doação no Banco
                    doacao = new Doacao();
                    doacao.setValor(novoPagamento.getValor());
                    doacao.setMoeda("BRL");
                    doacao.setStatusDoacao(mapearStatus(resultadoMP.getStatus()));
                    doacao.setCriadoEm(LocalDateTime.now());
                    doacao.setAtualizadoEm(LocalDateTime.now());
                    doacao.setReferenciaExt(ref);
                    doacao.setPagamentoId(String.valueOf(novoPagamento.getIdPagamento())); 
                    
                    // Tenta extrair o ID do usuário da string "ASSIN-{id}"
                    try {
                        String idStr = ref.replace("ASSIN-", "");
                        Long idUsuario = Long.parseLong(idStr);
                        // Aqui poderíamos buscar o id_assinatura desse usuário, mas 
                        // salvaremos a doação vinculada ao usuário indiretamente pela ref.
                        // Se quiser vincular direto na coluna id_assinatura, precisaria buscar a assinatura ativa do usuário.
                    } catch(Exception e) {
                        System.out.println("Não foi possível extrair ID de usuário da ref: " + ref);
                    }
                    
                    doacaoDAO.salvarDoacao(doacao);
                } else {
                    System.out.println("Doação não encontrada e ref desconhecida: " + ref);
                    return;
                }
            }

            // 5. Atualizar Tabela DOACAO (Status e Data)
            StatusDoacao statusNovo = mapearStatus(resultadoMP.getStatus());
            doacao.setStatusDoacao(statusNovo);
            doacao.setAtualizadoEm(LocalDateTime.now());
            doacaoDAO.atualizarDoacao(doacao);

            // 6. Atualizar Tabela PAGAMENTO (Status e GatewayID)
            try {
                if (doacao.getPagamentoId() != null) {
                    Long idPagamentoLocal = Long.parseLong(doacao.getPagamentoId());
                    StatusPagamento statusPagamentoNovo = mapearStatusPagamento(resultadoMP.getStatus());
                    pagamentoDAO.atualizarStatus(idPagamentoLocal, statusPagamentoNovo, resultadoMP.getPagamentoId());
                }
            } catch (NumberFormatException e) {
                System.err.println("Erro ao converter FK pagamento_id: " + doacao.getPagamentoId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro crítico ao processar webhook: " + e.getMessage());
        }
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
            case "refunded": return StatusPagamento.EXPIRADO;
            default: return StatusPagamento.PENDENTE;
        }
    }
}