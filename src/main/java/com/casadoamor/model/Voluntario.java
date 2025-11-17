package com.casadoamor.model;

import com.casadoamor.enums.StatusInscricao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Voluntario extends Usuario {

    private LocalDate dataInscricao;
    private StatusInscricao statusInscricao;
    
    // relacionamento (M-M com atributos)
    private List<VoluntarioAreaAtuacao> areasDeAtuacao = new ArrayList<>();

    // construtores
    public Voluntario() {
        super();
    }

    public Voluntario(long idUsuario, String nome, String email, String cpf, String telefone, LocalDate dataInscricao, StatusInscricao statusInscricao) {
        super(idUsuario, nome, email, cpf, telefone);
        this.dataInscricao = dataInscricao;
        this.statusInscricao = statusInscricao;
    }

    // getters e setters
    public LocalDate getDataInscricao() {
        return dataInscricao;
    }

    public void setDataInscricao(LocalDate dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    public StatusInscricao getStatusInscricao() {
        return statusInscricao;
    }

    public void setStatusInscricao(StatusInscricao statusInscricao) {
        this.statusInscricao = statusInscricao;
    }

    public List<VoluntarioAreaAtuacao> getAreasDeAtuacao() {
        return areasDeAtuacao;
    }

    public void setAreasDeAtuacao(List<VoluntarioAreaAtuacao> areasDeAtuacao) {
        this.areasDeAtuacao = areasDeAtuacao;
    }
}