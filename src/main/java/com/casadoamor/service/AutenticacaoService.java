package com.casadoamor.service;

import com.casadoamor.dao.AdministradorDAO;
import com.casadoamor.dto.LoginRequest;
import com.casadoamor.dto.LoginResponse;
import com.casadoamor.model.Administrador;
import java.util.UUID;

public class AutenticacaoService {

    private final AdministradorDAO administradorDAO;

    public AutenticacaoService() {
        this.administradorDAO = new AdministradorDAO();
    }

    public LoginResponse autenticar(LoginRequest login) throws Exception {
        // 1. Busca os dados no banco (Lembre-se: o DAO faz JOIN, então só traz se for Admin)
        Administrador admin = administradorDAO.buscarPorEmail(login.getEmail());

        // 2. Se admin for null, o email não existe ou não é um administrador
        if (admin == null) {
            throw new Exception("Credenciais inválidas ou usuário não é administrador.");
        }

        // 3. Verifica a senha (Comparação simples de String)
        if (admin.getSenhaHash() == null || !admin.getSenhaHash().equals(login.getSenha())) {
            throw new Exception("Credenciais inválidas.");
        }

        // 4. Gera um token simples (UUID) para simular uma sessão
        String token = UUID.randomUUID().toString();

        // 5. Retorna o DTO com as informações seguras para o front
        return new LoginResponse(
            admin.getIdUsuario(), 
            admin.getNome(), 
            "ADMIN", 
            token
        );
    }
}