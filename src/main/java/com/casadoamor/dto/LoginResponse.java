package com.casadoamor.dto;

public class LoginResponse {
    private Long id;
    private String nome;
    private String tipoUsuario; // "ADMIN" ou "VOLUNTARIO"
    private String token; 

    public LoginResponse(Long id, String nome, String tipoUsuario, String token) {
        this.id = id;
        this.nome = nome;
        this.tipoUsuario = tipoUsuario;
        this.token = token;
    }
    
    // Adiciona os Getters (Setters não são necessários na resposta imutável)
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTipoUsuario() { return tipoUsuario; }
    public String getToken() { return token; }
}
