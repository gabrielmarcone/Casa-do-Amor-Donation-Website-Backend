package com.casadoamor.doacao.gateway;

import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import com.casadoamor.doacao.config.MercadoPagoConfigManager;
import com.casadoamor.doacao.dto.CriarDoacaoRequest;
import com.casadoamor.doacao.model.Doacao;

public class MercadoPagoClient {

  private final WebClient webClient;

  public MercadoPagoClient() {

    String accessToken = MercadoPagoConfigManager.getPropriedades().getAccessToken();

    this.webClient = WebClient.builder()
        .baseUrl("https://api.mercadopago.com")
        .defaultHeader("Authorization", "Bearer " + accessToken)
        .build();
  }

  public PagamentoResultado criarPagamentoPix(Doacao doacao, CriarDoacaoRequest request) {

    // Corpo - Pagamento Via Pix
    var body = Map.of(
        "transaction_amount", doacao.getValor(),
        "description", "Doação para Casa do Amor",
        "payment_method_id", "pix",
        "external_reference", doacao.getReferenciaExt(),
        "notification_url", "https://seu-dominio.com/notifications",
        "payer", Map.of("email", doacao.getEmailDoador()));

    // Chamada à API do Mercado Pago
    var response = this.webClient.post()
        .uri("/v1/payments")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    // Extração dos dados do Pix da resposta  
    Map<String, Object> pointOfInteraction = (Map<String, Object>) response.get("point_of_interaction");
    Map<String, Object> pixData = (Map<String, Object>) pointOfInteraction.get("pix_data");

    PagamentoResultado resultado = new PagamentoResultado();
    resultado.setPagamentoId(response.get("id").toString());
    resultado.setQrCode(pixData.get("qr_code").toString());
    resultado.setQrCodeImg(pixData.get("qr_code_base64").toString());
    resultado.setMensagem("Pix criadoc om sucesso");

    return resultado;
  }

  public PagamentoResultado criarPagamentoCartao(Doacao doacao, CriarDoacaoRequest request) {
    // ...
    return null;
  }

  public PagamentoResultado consultarPagamento(String pagamentoId) {
    // ...
    return null;
  }

}
