package com.casadoamor.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Doador extends Usuario {
    
    private LocalDate dataCadastro;
    
    // relacionamentos (para facilitar o uso no serviço)
    private List<Doacao> doacoes = new ArrayList<>();
    private List<Assinatura> assinaturas = new ArrayList<>();

    // construtores
    public Doador() {
        super();
    }

    public Doador(long idUsuario, String nome, String email, String cpf, String telefone, LocalDate dataCadastro) {
        super(idUsuario, nome, email, cpf, telefone);
        this.dataCadastro = dataCadastro;
    }

    // getters e setters
    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public List<Doacao> getDoacoes() {
        return doacoes;
    }

    public void setDoacoes(List<Doacao> doacoes) {
        this.doacoes = doacoes;
    }

    public List<Assinatura> getAssinaturas() {
        return assinaturas;
    }

    public void setAssinaturas(List<Assinatura> assinaturas) {
        this.assinaturas = assinaturas;
    }
}