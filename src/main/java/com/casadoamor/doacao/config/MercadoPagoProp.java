package com.casadoamor.doacao.config;

public class MercadoPagoProp {

  private String accessToken;
  private String webhook;

  public MercadoPagoProp() {

  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getWebhook() {
    return webhook;
  }

  public void setWebhook(String webhook) {
    this.webhook = webhook;
  }

}
