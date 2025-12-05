package com.casadoamor.service;

import com.casadoamor.dao.AdministradorDAO;
import com.casadoamor.dto.LoginRequest;
import com.casadoamor.dto.LoginResponse;
import com.casadoamor.model.Administrador;
import org.mindrot.jbcrypt.BCrypt; // Import da nova biblioteca

import java.util.UUID;

public class AutenticacaoService {

    private final AdministradorDAO administradorDAO;

    public AutenticacaoService() {
        this.administradorDAO = new AdministradorDAO();
    }

    public LoginResponse autenticar(LoginRequest login) throws Exception {
        // 1. Busca os dados no banco pelo email
        Administrador admin = administradorDAO.buscarPorEmail(login.getEmail());

        // Se admin for null, o email não existe ou não é um administrador
        if (admin == null) {
            throw new Exception("Credenciais inválidas ou usuário não é administrador.");
        }

        // 2. Verifica a senha usando BCrypt (Segurança)
        // O checkpw pega a senha "1234" digitada e compara matematicamente com o hash "$2a$10$..." do banco
        if (admin.getSenhaHash() == null || !BCrypt.checkpw(login.getSenha(), admin.getSenhaHash())) {
            throw new Exception("Credenciais inválidas.");
        }

        // Gera um token simples (UUID) para simular uma sessão
        String token = UUID.randomUUID().toString();

        return new LoginResponse(
            admin.getIdUsuario(), 
            admin.getNome(), 
            "ADMIN", 
            token
        );
    }
    
    // DICA: Use este método auxiliar num "Main" separado apenas para gerar senhas para inserir no banco manualmente
    public static String gerarHashParaTeste(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt());
    }
}