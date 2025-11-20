package com.casadoamor.model;

import com.casadoamor.enums.NivelAcesso;
import com.casadoamor.enums.StatusUsuario;

public class Administrador extends Usuario {
    
    private String senhaHash;
    private StatusUsuario status;
    private NivelAcesso nivelAcesso;

    // construtores
    public Administrador() {
        super();
    }

    public Administrador(long idUsuario, String nome, String email, String cpf, String telefone, String senhaHash, StatusUsuario status, NivelAcesso nivelAcesso) {
        super(idUsuario, nome, email, cpf, telefone);
        this.senhaHash = senhaHash;
        this.status = status;
        this.nivelAcesso = nivelAcesso;
    }

    // getters e setters
    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public NivelAcesso getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(NivelAcesso nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}