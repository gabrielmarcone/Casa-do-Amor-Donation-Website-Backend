package com.casadoamor.model;

import com.casadoamor.enums.TipoDoacao;
import java.time.LocalDateTime;

public class Doacao {
    
    private long idDoacao;
    private TipoDoacao tipoDoacao;
    private LocalDateTime dataDoacao;
    
    // relacionamentos (chaves estrangeiras)
    private Doador doador; // (realiza)
    private Pagamento pagamento; 
    private Assinatura assinatura; 

    // construtores
    public Doacao() {
    }

    public Doacao(long idDoacao, TipoDoacao tipoDoacao, LocalDateTime dataDoacao, Doador doador, Pagamento pagamento, Assinatura assinatura) {
        this.idDoacao = idDoacao;
        this.tipoDoacao = tipoDoacao;
        this.dataDoacao = dataDoacao;
        this.doador = doador;
        this.pagamento = pagamento;
        this.assinatura = assinatura;
    }

    // getters e setters
    public long getIdDoacao() {
        return idDoacao;
    }

    public void setIdDoacao(long idDoacao) {
        this.idDoacao = idDoacao;
    }

    public TipoDoacao getTipoDoacao() {
        return tipoDoacao;
    }

    public void setTipoDoacao(TipoDoacao tipoDoacao) {
        this.tipoDoacao = tipoDoacao;
    }

    public LocalDateTime getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDateTime dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public Doador getDoador() {
        return doador;
    }

    public void setDoador(Doador doador) {
        this.doador = doador;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public Assinatura getAssinatura() {
        return assinatura;
    }

void setAssinatura(Assinatura assinatura) {
        this.assinatura = assinatura;
    }
}