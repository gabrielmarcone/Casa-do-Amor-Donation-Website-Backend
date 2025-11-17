package com.casadoamor.model;

public class AreaAtuacao {
    
    private long idArea;
    private String nome;

    // construtores
    public AreaAtuacao() {
    }

    public AreaAtuacao(long idArea, String nome) {
        this.idArea = idArea;
        this.nome = nome;
    }

    // getters e setters
    public long getIdArea() {
        return idArea;
    }

    public void setIdArea(long idArea) {
        this.idArea = idArea;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}