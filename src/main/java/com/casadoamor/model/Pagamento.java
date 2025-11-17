package com.casadoamor.model;

import com.casadoamor.enums.MetodoPagamento;
import com.casadoamor.enums.StatusPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pagamento {

    private long idPagamento;
    private LocalDateTime dataPagamento;
    private BigDecimal valor;
    private MetodoPagamento metodoPagamento;
    private StatusPagamento statusPagamento;

    // construtores
    public Pagamento() {
    }

    public Pagamento(long idPagamento, LocalDateTime dataPagamento, BigDecimal valor, MetodoPagamento metodoPagamento, StatusPagamento statusPagamento) {
        this.idPagamento = idPagamento;
        this.dataPagamento = dataPagamento;
        this.valor = valor;
        this.metodoPagamento = metodoPagamento;
        this.statusPagamento = statusPagamento;
    }

    // getters e setters
    public long getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(long idPagamento) {
        this.idPagamento = idPagamento;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public MetodoPagamento getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(MetodoPagamento metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento;
    }
}