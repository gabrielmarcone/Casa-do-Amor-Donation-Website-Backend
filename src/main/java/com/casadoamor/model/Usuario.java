package com.casadoamor.model;

import java.io.Serializable;

public class Usuario implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private long idUsuario;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;

    // construtores
    public Usuario() {
    }

    public Usuario(long idUsuario, String nome, String email, String cpf, String telefone) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    // getters e setters
    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}