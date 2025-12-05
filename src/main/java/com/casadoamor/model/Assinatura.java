package com.casadoamor.model;

import com.casadoamor.enums.StatusAssinatura;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Assinatura {
    private Long idAssinatura;
    private Long idUsuarioDoador; // FK para Doador
    private StatusAssinatura statusAssinatura;
    private int diaCobranca;
    private BigDecimal valorMensal;
    private LocalDate dataInicio;
    private String idAssinaturaGateway; // ID da assinatura no MercadoPago (Preapproval ID)

    public Assinatura() {}

    // Getters e Setters...
    public Long getIdAssinatura() { return idAssinatura; }
    public void setIdAssinatura(Long idAssinatura) { this.idAssinatura = idAssinatura; }
    public Long getIdUsuarioDoador() { return idUsuarioDoador; }
    public void setIdUsuarioDoador(Long idUsuarioDoador) { this.idUsuarioDoador = idUsuarioDoador; }
    public StatusAssinatura getStatusAssinatura() { return statusAssinatura; }
    public void setStatusAssinatura(StatusAssinatura statusAssinatura) { this.statusAssinatura = statusAssinatura; }
    public int getDiaCobranca() { return diaCobranca; }
    public void setDiaCobranca(int diaCobranca) { this.diaCobranca = diaCobranca; }
    public BigDecimal getValorMensal() { return valorMensal; }
    public void setValorMensal(BigDecimal valorMensal) { this.valorMensal = valorMensal; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public String getIdAssinaturaGateway() { return idAssinaturaGateway; }
    public void setIdAssinaturaGateway(String idAssinaturaGateway) { this.idAssinaturaGateway = idAssinaturaGateway; }
}