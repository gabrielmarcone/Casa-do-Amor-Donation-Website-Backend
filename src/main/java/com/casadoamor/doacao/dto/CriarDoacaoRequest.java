package com.casadoamor.doacao.dto;

import java.math.BigDecimal;

public class CriarDoacaoRequest {

  private BigDecimal valor;
  private String descricao;

  private String metodoPagamento;

  private String nomeDoador;
  private String emailDoador;

  // Para cartao
  private String tokenCartao; // token gerado no front pelo gateway
  private Integer parcelas;
  private String metodoPagamentoId; // ex: visa, master, elo
  private String tipoPagamento; // credit_card ou debit_card
  private String documentoTipo; // ex: CPF
  private String documentoNumero; // ex: 12345678909

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

  public String getTokenCartao() {
    return tokenCartao;
  }

  public void setTokenCartao(String tokenCartao) {
    this.tokenCartao = tokenCartao;
  }

  public Integer getParcelas() {
    return parcelas;
  }

  public void setParcelas(Integer parcelas) {
    this.parcelas = parcelas;
  }

  public String getMetodoPagamentoId() {
    return metodoPagamentoId;
  }

  public void setMetodoPagamentoId(String metodoPagamentoId) {
    this.metodoPagamentoId = metodoPagamentoId;
  }

  public String getTipoPagamento() {
    return tipoPagamento;
  }

  public void setTipoPagamento(String tipoPagamento) {
    this.tipoPagamento = tipoPagamento;
  }

  public String getDocumentoTipo() {
    return documentoTipo;
  }

  public void setDocumentoTipo(String documentoTipo) {
    this.documentoTipo = documentoTipo;
  }

  public String getDocumentoNumero() {
    return documentoNumero;
  }

  public void setDocumentoNumero(String documentoNumero) {
    this.documentoNumero = documentoNumero;
  }

}
