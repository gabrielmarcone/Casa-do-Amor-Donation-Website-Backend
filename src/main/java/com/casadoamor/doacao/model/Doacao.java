package com.casadoamor.doacao.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.casadoamor.doacao.enums.StatusDoacao;

public class Doacao {
  private Long id;

  private BigDecimal valor;
  private String moeda;

  private StatusDoacao statusDoacao;

  private String referenciaExt; // id enviado ao gateway de pagamento
  private String pagamentoId; // id retornado pelo gateway de pagamento
  private String idempotencyKey; // evitar duplicidade de transações

  private String nomeDoador;
  private String emailDoador;

  private LocalDateTime criadoEm;
  private LocalDateTime atualizadoEm;

  public Doacao() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public BigDecimal getValor() {
    return valor;
  }

  public void setValor(BigDecimal valor) {
    this.valor = valor;
  }

  public String getMoeda() {
    return moeda;
  }

  public void setMoeda(String moeda) {
    this.moeda = moeda;
  }

  public StatusDoacao getStatusDoacao() {
    return statusDoacao;
  }

  public void setStatusDoacao(StatusDoacao statusDoacao) {
    this.statusDoacao = statusDoacao;
  }

  public String getReferenciaExt() {
    return referenciaExt;
  }

  public void setReferenciaExt(String referenciaExt) {
    this.referenciaExt = referenciaExt;
  }

  public String getPagamentoId() {
    return pagamentoId;
  }

  public void setPagamentoId(String pagamentoId) {
    this.pagamentoId = pagamentoId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
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

  public LocalDateTime getCriadoEm() {
    return criadoEm;
  }

  public void setCriadoEm(LocalDateTime criadoEm) {
    this.criadoEm = criadoEm;
  }

  public LocalDateTime getAtualizadoEm() {
    return atualizadoEm;
  }

  public void setAtualizadoEm(LocalDateTime atualizadoEm) {
    this.atualizadoEm = atualizadoEm;
  }

}
