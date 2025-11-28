package com.casadoamor.doacao.dto;

public class CriarDoacaoResponse {

  private Long id; // id interno para doação
  private String status; // status da doação
  private String metodoPagamento; // método de pagamento utilizado

  // Pix
  private String qrCode; // string do código QR
  private String qrCodeImg; // imagem do código QR

  // Cartão
  private String pagamentoId; // id do pagamento
  private String mensagem; // resposta do gateway de pagamento

  public CriarDoacaoResponse() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getMetodoPagamento() {
    return metodoPagamento;
  }

  public void setMetodoPagamento(String metodoPagamento) {
    this.metodoPagamento = metodoPagamento;
  }

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public String getQrCodeImg() {
    return qrCodeImg;
  }

  public void setQrCodeImg(String qrCodeImg) {
    this.qrCodeImg = qrCodeImg;
  }

  public String getPagamentoId() {
    return pagamentoId;
  }

  public void setPagamentoId(String pagamentoId) {
    this.pagamentoId = pagamentoId;
  }

  public String getMensagem() {
    return mensagem;
  }

  public void setMensagem(String mensagem) {
    this.mensagem = mensagem;
  }

}
