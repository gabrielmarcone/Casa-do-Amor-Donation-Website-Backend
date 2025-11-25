package com.casadoamor.doacao.gateway;

public class PagamentoResultado {

  private String pagamentoId;
  private String qrCode;
  private String qrCodeImg;
  private String mensagem;
  private String referenciaExt;
  private String status;

  public PagamentoResultado() {
  }

  public String getPagamentoId() {
    return pagamentoId;
  }

  public void setPagamentoId(String pagamentoId) {
    this.pagamentoId = pagamentoId;
  }

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public String getReferenciaExt() {
    return referenciaExt;
  }

  public void setReferenciaExt(String referenciaExt) {
    this.referenciaExt = referenciaExt;
  }

  public String getQrCodeImg() {
    return qrCodeImg;
  }

  public void setQrCodeImg(String qrCodeImg) {
    this.qrCodeImg = qrCodeImg;
  }

  public String getMensagem() {
    return mensagem;
  }

  public void setMensagem(String mensagem) {
    this.mensagem = mensagem;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
