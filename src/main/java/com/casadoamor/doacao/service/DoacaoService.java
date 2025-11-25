package com.casadoamor.doacao.service;

import java.time.LocalDateTime;
import java.util.UUID;

import com.casadoamor.doacao.config.MercadoPagoConfigManager;
import com.casadoamor.doacao.dao.DoacaoDAO;
import com.casadoamor.doacao.dto.CriarDoacaoRequest;
import com.casadoamor.doacao.dto.CriarDoacaoResponse;
import com.casadoamor.doacao.enums.StatusDoacao;
import com.casadoamor.doacao.gateway.MercadoPagoClient;
import com.casadoamor.doacao.gateway.PagamentoResultado;
import com.casadoamor.doacao.model.Doacao;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DoacaoService {

  DoacaoDAO doacaoDAO = new DoacaoDAO();

  private MercadoPagoClient mercadoPagoClient = new MercadoPagoClient();

  public CriarDoacaoResponse criarDoacao(CriarDoacaoRequest request) {

    // Criando a doação
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

    PagamentoResultado resultado = null;
    if (request.getMetodoPagamento().equalsIgnoreCase("PIX")) {
      resultado = mercadoPagoClient.criarPagamentoPix(doacao, request);
    }

    CriarDoacaoResponse response = new CriarDoacaoResponse();
    response.setId(doacao.getId());
    response.setStatus(doacao.getStatusDoacao().name());
    response.setMetodoPagamento(request.getMetodoPagamento());
    response.setQrCode(resultado.getQrCode());
    response.setQrCodeImg(resultado.getQrCodeImg());
    response.setMensagem(resultado.getMensagem());

    return response;
  }

  public Doacao buscarPorId(Long id) {
    return doacaoDAO.buscarPorId(id);

  }

  public void processarWebhook(String payload, String signature, String requestId) {

    JSONObject jsonObject = new JSONObject(payload);
    Long pagamentoId = jsonObject.getLong("data.id");

    String secret = MercadoPagoConfigManager.getPropriedades().getWebhook();

    String mensagem = "id:" + pagamentoId + ";requestId:" + requestId;

    String assinaturaCalculada = hmacSha256(mensagem, secret);

    if(!assinaturaCalculada.equals(signature)){
      System.out.println("Assinatura inválida no webhook");
      return;
    }


    PagamentoResultado resultado = mercadoPagoClient.consultarPagamento(pagamentoId.toString());

    Doacao doacao = doacaoDAO.buscaPorRefEx(resultado.getReferenciaExt());

    // ... 

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

}
