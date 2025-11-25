package com.casadoamor.doacao.dto;

import java.math.BigDecimal;

public class CriarDoacaoRequest {

  private BigDecimal valor;
  private String descricao;

  private String metodoPagamento;

  private String nomeDoador;
  private String emailDoador;

  // Para cartão
  private String cardToken; // token gerado no front pelo gateway
  private Integer parcelas;

  public CriarDoacaoRequest() {
  }

  public BigDecimal getValor() {
    return valor;
  }

  public void setValor(BigDecimal valor) {
    this.valor = valor;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  public String getMetodoPagamento() {
    return metodoPagamento;
  }

  public void setMetodoPagamento(String metodoPagamento) {
    this.metodoPagamento = metodoPagamento;
  }

  public String getNomeDoador() {
    return nomeDoador;
  }

  public void setNomeDoador(String nomeDoador) {
    this.nomeDoador = nomeDoador;
  }

  public String getEmailDoador() {
    return emailDoador;
  }

  public void setEmailDoador(String emailDoador) {
    this.emailDoador = emailDoador;
  }

  public String getCardToken() {
    return cardToken;
  }

  public void setCardToken(String cardToken) {
    this.cardToken = cardToken;
  }

  public Integer getParcelas() {
    return parcelas;
  }

  public void setParcelas(Integer parcelas) {
    this.parcelas = parcelas;
  }

}
