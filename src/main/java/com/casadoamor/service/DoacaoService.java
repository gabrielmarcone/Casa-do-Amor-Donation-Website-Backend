package com.casadoamor.service;

import com.casadoamor.dao.DoacaoDAO;
import com.casadoamor.dao.PagamentoDAO;
import com.casadoamor.doacao.gateway.MercadoPagoClient;
import com.casadoamor.doacao.gateway.PagamentoResultado;
import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.dto.CriarDoacaoResponse;
import com.casadoamor.enums.MetodoPagamento;
import com.casadoamor.enums.StatusDoacao;
import com.casadoamor.enums.StatusPagamento;
import com.casadoamor.model.Doacao;
import com.casadoamor.model.Pagamento;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DoacaoService {

    private final DoacaoDAO doacaoDAO = new DoacaoDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO(); // Novo DAO
    private final MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();

    public CriarDoacaoResponse criarDoacao(CriarDoacaoRequest request) {
        // 1. Preparar e Salvar o Pagamento primeiro (Status Pendente)
        Pagamento pagamento = new Pagamento();
        pagamento.setValor(request.getValor());
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setStatusPagamento(StatusPagamento.PENDENTE);
        
        // Converte string do request para Enum (ajuste conforme seu Enum exato)
        try {
            pagamento.setMetodoPagamento(MetodoPagamento.valueOf(request.getMetodoPagamento().toUpperCase())); 
        } catch (Exception e) {
             // Fallback ou erro se o metodo não bater
             pagamento.setMetodoPagamento(MetodoPagamento.PIX); 
        }
        
        // Salva no banco para gerar o ID
        pagamento = pagamentoDAO.salvar(pagamento);

        // 2. Preparar e Salvar a Doação vinculada ao Pagamento
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
        
        // VINCULA O PAGAMENTO (Importante para o Modelo Relacional)
        doacao.setPagamentoId(String.valueOf(pagamento.getIdPagamento())); 

        doacaoDAO.salvarDoacao(doacao);

        // 3. Chamar o Gateway (Mercado Pago)
        PagamentoResultado resultado = null;
        if (pagamento.getMetodoPagamento() == MetodoPagamento.PIX) {
            resultado = mercadoPagoClient.criarPagamentoPix(doacao, request);
        } else {
            // Assumindo cartao
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

    // ... Manter os outros métodos (buscarPorId, webhook) adaptando para usar pagamentoDAO quando necessário
    public Doacao buscarPorId(Long id) {
        return doacaoDAO.buscarPorId(id);
    }
    
    // Método webhook omitido por brevidade, mas deve atualizar tanto PagamentoDAO quanto DoacaoDAO
    public void processarWebhook(String payload, String signature, String requestId) {
        // ... (lógica existente mantida, apenas lembre de atualizar o status na tabela Pagamento tbm)
    }
}