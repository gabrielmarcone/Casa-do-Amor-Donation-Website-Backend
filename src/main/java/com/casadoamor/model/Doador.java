package com.casadoamor.model;

import java.time.LocalDate;

public class Doador extends Usuario {

    private LocalDate dataCadastro;

    public Doador() {
        super();
    }

    public Doador(long idUsuario, String nome, String email, String cpf, String telefone, LocalDate dataCadastro) {
        super(idUsuario, nome, email, cpf, telefone);
        this.dataCadastro = dataCadastro;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}