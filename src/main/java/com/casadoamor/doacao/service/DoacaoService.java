package com.casadoamor.doacao.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List; // Import necessário
import java.util.UUID;

import com.casadoamor.doacao.config.MercadoPagoConfigManager;
import com.casadoamor.doacao.dao.DoacaoDAO;
import com.casadoamor.doacao.dto.CriarDoacaoRequest;
import com.casadoamor.doacao.dto.CriarDoacaoResponse;
import com.casadoamor.doacao.enums.StatusDoacao;
import com.casadoamor.doacao.gateway.MercadoPagoClient;
import com.casadoamor.doacao.gateway.PagamentoResultado;
import com.casadoamor.doacao.model.Doacao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class DoacaoService {

  DoacaoDAO doacaoDAO = new DoacaoDAO();

  private MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  // --- NOVO MÉTODO PARA LISTAR TUDO (USADO NO ADMIN) ---
  public List<Doacao> listarTodas() {
      return doacaoDAO.listarTodas();
  }
  // -----------------------------------------------------

  public CriarDoacaoResponse criarDoacao(CriarDoacaoRequest request) {

    Doacao doacao = new Doacao();
    doacao.setValor(request.getValor());
    doacao.setMoeda("BRL");
    doacao.setStatusDoacao(StatusDoacao.PENDING);
    doacao.setNomeDoador(request.getNomeDoador());
    doacao.setEmailDoador(request.getEmailDoador());

    String referenciaExterna = UUID.randomUUID().toString();
    String idempotencyKey = UUID.randomUUID().toString();

    doacao.setReferenciaExt(referenciaExterna);
    doacao.setIdempotencyKey(idempotencyKey);

    doacao.setCriadoEm(LocalDateTime.now());
    doacao.setAtualizadoEm(LocalDateTime.now());

    doacaoDAO.salvarDoacao(doacao);

    String tipoPagamento = request.getTipoPagamento(); 
    if (tipoPagamento == null) {
        tipoPagamento = request.getMetodoPagamento();
    }
    if (tipoPagamento == null) {
        tipoPagamento = ""; 
    }

    PagamentoResultado resultado = null;

    if ("PIX".equalsIgnoreCase(tipoPagamento)) {
      resultado = mercadoPagoClient.criarPagamentoPix(doacao, request);
    } 
    else if ("credit_card".equalsIgnoreCase(tipoPagamento)
        || "cartao".equalsIgnoreCase(tipoPagamento)
        || "cartao_credito".equalsIgnoreCase(tipoPagamento)
        || "cartao_debito".equalsIgnoreCase(tipoPagamento)) {
      resultado = mercadoPagoClient.criarPagamentoCartao(doacao, request);
    } 
    else {
        System.out.println("Tipo de pagamento desconhecido: " + tipoPagamento);
    }

    if (resultado != null) {
      doacao.setPagamentoId(resultado.getPagamentoId());
      doacao.setStatusDoacao(mapearStatus(resultado.getStatus()));
      doacao.setAtualizadoEm(LocalDateTime.now());
      doacaoDAO.atualizarDoacao(doacao);
    }

    CriarDoacaoResponse response = new CriarDoacaoResponse();
    response.setId(doacao.getId());
    response.setStatus(doacao.getStatusDoacao().name());
    response.setMetodoPagamento(tipoPagamento);
    
    if (resultado != null) {
      response.setQrCode(resultado.getQrCode());
      response.setQrCodeImg(resultado.getQrCodeImg());
      response.setPagamentoId(resultado.getPagamentoId());
      response.setMensagem(resultado.getMensagem());
    } else {
      response.setMensagem("Metodo de pagamento nao suportado ou invalido: " + tipoPagamento);
    }

    return response;
  }

  public Doacao buscarPorId(Long id) {
    return doacaoDAO.buscarPorId(id);
  }

  public void processarWebhook(String payload, String signature, String requestId) {

    try {
      JsonNode root = objectMapper.readTree(payload);
      JsonNode data = root.path("data");

      Long pagamentoId = null;
      if (data != null && !data.isMissingNode()) {
        long id = data.path("id").asLong(0L);
        if (id > 0) {
          pagamentoId = id;
        }
      }
      if (pagamentoId == null) {
        long id = root.path("data.id").asLong(0L);
        if (id > 0) {
          pagamentoId = id;
        }
      }

      if (pagamentoId == null || pagamentoId == 0L) {
        System.out.println("Webhook sem id de pagamento");
        return;
      }

      String secret = MercadoPagoConfigManager.getPropriedades().getWebhook();

      String mensagem = "id:" + pagamentoId + ";requestId:" + requestId;
      String assinaturaCalculada = hmacSha256(mensagem, secret);

      if (!assinaturaCalculada.equals(signature)) {
        System.out.println("Assinatura invalida no webhook");
        return;
      }

      PagamentoResultado resultado = mercadoPagoClient.consultarPagamento(pagamentoId.toString());
      if (resultado == null) {
        System.out.println("Pagamento nao encontrado no gateway");
        return;
      }

      Doacao doacao = doacaoDAO.buscaPorRefEx(resultado.getReferenciaExt());
      if (doacao == null) {
        System.out.println("Doacao nao encontrada para referencia: " + resultado.getReferenciaExt());
        return;
      }

      doacao.setPagamentoId(resultado.getPagamentoId());
      doacao.setStatusDoacao(mapearStatus(resultado.getStatus()));
      doacao.setAtualizadoEm(LocalDateTime.now());

      doacaoDAO.atualizarDoacao(doacao);
      doacaoDAO.atualizarStatusDoacao(doacao.getId(), doacao.getStatusDoacao());

    } catch (Exception e) {
      System.out.println("Erro ao processar webhook: " + e.getMessage());
    }
  }

  private String hmacSha256(String mensagem, String segredo) {
    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec keySpec = new SecretKeySpec(segredo.getBytes(), "HmacSHA256");
      sha256_HMAC.init(keySpec);

      byte[] hash = sha256_HMAC.doFinal(mensagem.getBytes());

      return Base64.getEncoder().encodeToString(hash);

    } catch (Exception e) {
      throw new RuntimeException("Erro ao calcular assinatura HMAC");
    }
  }

  private StatusDoacao mapearStatus(String statusGateway) {
    if (statusGateway == null) {
      return StatusDoacao.PENDING;
    }

    switch (statusGateway.toLowerCase()) {
      case "approved":
        return StatusDoacao.PAID;
      case "in_process":
      case "in_mediation":
        return StatusDoacao.REQUIRES_ACTION;
      case "pending":
      case "authorized":
        return StatusDoacao.PENDING;
      case "rejected":
        return StatusDoacao.FAILED;
      case "cancelled":
      case "refunded":
        return StatusDoacao.CANCELLED;
      default:
        return StatusDoacao.PENDING;
    }
  }

} 