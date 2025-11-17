package com.casadoamor.model;

import com.casadoamor.enums.StatusAssinatura;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Assinatura {
    
    private long idAssinatura;
    private int diaCobranca;
    private BigDecimal valorMensal;
    private LocalDate dataInicio;
    private StatusAssinatura statusAssinatura;
    private String idAssinaturaGateway; // ID do gateway de pagamento (ex: Stripe, PagSeguro)

    // relacionamento (dono da assinatura)
    private Doador doador;
    
    // relacionamento (doacoes geradas por esta assinatura)
    private List<Doacao> doacoesGeradas = new ArrayList<>();

    // construtores
    public Assinatura() {
    }

    public Assinatura(long idAssinatura, int diaCobranca, BigDecimal valorMensal, LocalDate dataInicio, StatusAssinatura statusAssinatura, String idAssinaturaGateway, Doador doador) {
        this.idAssinatura = idAssinatura;
        this.diaCobranca = diaCobranca;
        this.valorMensal = valorMensal;
        this.dataInicio = dataInicio;
        this.statusAssinatura = statusAssinatura;
        this.idAssinaturaGateway = idAssinaturaGateway;
        this.doador = doador;
    }

    // getters e setters
    public long getIdAssinatura() {
        return idAssinatura;
    }

    public void setIdAssinatura(long idAssinatura) {
        this.idAssinatura = idAssinatura;
    }

    public int getDiaCobranca() {
        return diaCobranca;
    }

    public void setDiaCobranca(int diaCobranca) {
        this.diaCobranca = diaCobranca;
    }

    public BigDecimal getValorMensal() {
        return valorMensal;
    }

    public void setValorMensal(BigDecimal valorMensal) {
        this.valorMensal = valorMensal;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public StatusAssinatura getStatusAssinatura() {
        return statusAssinatura;
    }

    public void setStatusAssinatura(StatusAssinatura statusAssinatura) {
        this.statusAssinatura = statusAssinatura;
    }

    public String getIdAssinaturaGateway() {
        return idAssinaturaGateway;
    }

    public void setIdAssinaturaGateway(String idAssinaturaGateway) {
        this.idAssinaturaGateway = idAssinaturaGateway;
    }

    public Doador getDoador() {
        return doador;
    }

    public void setDoador(Doador doador) {
        this.doador = doador;
    }

    public List<Doacao> getDoacoesGeradas() {
        return doacoesGeradas;
    }

    public void setDoacoesGeradas(List<Doacao> doacoesGeradas) {
        this.doacoesGeradas = doacoesGeradas;
    }
}