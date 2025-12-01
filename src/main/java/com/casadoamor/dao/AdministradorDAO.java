package com.casadoamor.dao;

import com.casadoamor.model.Administrador;
import com.casadoamor.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdministradorDAO {

    /**
     * Busca um Administrador completo pelo email (que está na tabela pai).
     */
    public Administrador buscarPorEmail(String email) {
        // Faz o JOIN para pegar dados do Usuário (nome, email) e do Admin (senha)
        // Ajuste os nomes das tabelas/colunas conforme seu banco exato
        String sql = "SELECT u.id_usuario, u.nome, u.email, a.senha " +
                     "FROM Usuario u " +
                     "JOIN Administrador a ON u.id_usuario = a.id_usuario " +
                     "WHERE u.email = ?";

        Administrador admin = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            
            pstm.setString(1, email);
            
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    admin = new Administrador();
                    
                    // Dados da tabela Pai (Usuario)
                    admin.setIdUsuario(rs.getLong("id_usuario"));
                    admin.setNome(rs.getString("nome"));
                    admin.setEmail(rs.getString("email"));
                    
                    // Dados da tabela Filha (Administrador)
                    admin.setSenhaHash(rs.getString("senha"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar administrador: " + e.getMessage());
        }
        return admin;
    }
}