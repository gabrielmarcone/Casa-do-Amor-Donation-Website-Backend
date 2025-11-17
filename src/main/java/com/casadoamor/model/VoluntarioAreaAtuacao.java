package com.casadoamor.model;

public class VoluntarioAreaAtuacao {
    
    // chaves estrangeiras
    private Voluntario voluntario;
    private AreaAtuacao areaAtuacao;
    
    // atributos da relacao
    private String especialidade;
    private String disponibilidade;

    public VoluntarioAreaAtuacao() {
    }

    public VoluntarioAreaAtuacao(Voluntario voluntario, AreaAtuacao areaAtuacao, String especialidade, String disponibilidade) {
        this.voluntario = voluntario;
        this.areaAtuacao = areaAtuacao;
        this.especialidade = especialidade;
        this.disponibilidade = disponibilidade;
    }

    public Voluntario getVoluntario() {
        return voluntario;
    }

    public void setVoluntario(Voluntario voluntario) {
        this.voluntario = voluntario;
    }

    public AreaAtuacao getAreaAtuacao() {
        return areaAtuacao;
    }

    public void setAreaAtuacao(AreaAtuacao areaAtuacao) {
        this.areaAtuacao = areaAtuacao;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(String disponibilidade) {
        this.disponibilidade = disponibilidade;
    }
}