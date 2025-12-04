package com.casadoamor.model;

import com.casadoamor.enums.MetodoPagamento;
import com.casadoamor.enums.StatusPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pagamento {
    private Long idPagamento;
    private BigDecimal valor;
    private MetodoPagamento metodoPagamento;
    private LocalDateTime dataPagamento;
    private StatusPagamento statusPagamento;
    private String gatewayId; // ID que vem do Mercado Pago

    public Pagamento() {}

    // Getters e Setters
    public Long getIdPagamento() { return idPagamento; }
    public void setIdPagamento(Long idPagamento) { this.idPagamento = idPagamento; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public MetodoPagamento getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamento metodoPagamento) { this.metodoPagamento = metodoPagamento; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }
    public String getGatewayId() { return gatewayId; }
    public void setGatewayId(String gatewayId) { this.gatewayId = gatewayId; }
}