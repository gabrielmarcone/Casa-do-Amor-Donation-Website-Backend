package com.casadoamor.dao;

import com.casadoamor.model.Doador;
import com.casadoamor.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoadorDAO {

    public void salvar(Doador doador) {
        // SQL para a tabela Pai
        String sqlUsuario = "INSERT INTO usuario (nome, email, cpf, telefone) VALUES (?, ?, ?, ?)";
        // SQL para a tabela Filha (Doador só tem ID e data_cadastro, conforme DER)
        String sqlDoador = "INSERT INTO doador (id_usuario, data_cadastro) VALUES (?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // Inicia Transação

            try {
                // 1. Salvar Usuario e recuperar ID
                Long idUsuarioGerado = null;
                try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, doador.getNome());
                    psUser.setString(2, doador.getEmail());
                    psUser.setString(3, doador.getCpf());
                    psUser.setString(4, doador.getTelefone());
                    psUser.executeUpdate();

                    try (ResultSet rs = psUser.getGeneratedKeys()) {
                        if (rs.next()) {
                            idUsuarioGerado = rs.getLong(1);
                            doador.setIdUsuario(idUsuarioGerado);
                        } else {
                            throw new SQLException("Falha ao obter ID do usuário para o doador.");
                        }
                    }
                }

                // 2. Salvar Doador vinculado
                try (PreparedStatement psDoador = conn.prepareStatement(sqlDoador)) {
                    psDoador.setLong(1, idUsuarioGerado);
                    // Se a data vier nula, usa a data atual
                    Date dataCadastro = doador.getDataCadastro() != null ? Date.valueOf(doador.getDataCadastro()) : new Date(System.currentTimeMillis());
                    psDoador.setDate(2, dataCadastro);
                    
                    psDoador.executeUpdate();
                }

                conn.commit(); // Confirma a transação

            } catch (SQLException e) {
                conn.rollback(); // Desfaz tudo se der erro
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar doador (sócio)", e);
        }
    }

    // Listar Sócios-Doadores (Feature 1.4 do DER)
    public List<Doador> listar() {
        String sql = "SELECT u.id_usuario, u.nome, u.email, u.cpf, u.telefone, d.data_cadastro " +
                     "FROM doador d " +
                     "JOIN usuario u ON d.id_usuario = u.id_usuario";
        
        List<Doador> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Doador d = new Doador();
                d.setIdUsuario(rs.getLong("id_usuario"));
                d.setNome(rs.getString("nome"));
                d.setEmail(rs.getString("email"));
                d.setCpf(rs.getString("cpf"));
                d.setTelefone(rs.getString("telefone"));
                d.setDataCadastro(rs.getDate("data_cadastro").toLocalDate());
                
                lista.add(d);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar sócios-doadores", e);
        }
        return lista;
    }
    
    // Método auxiliar para buscar por email (útil para login ou verificar duplicidade)
    public Doador buscarPorEmail(String email) {
        String sql = "SELECT u.id_usuario, u.nome, u.email, u.cpf, u.telefone, d.data_cadastro " +
                     "FROM doador d " +
                     "JOIN usuario u ON d.id_usuario = u.id_usuario " +
                     "WHERE u.email = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Doador d = new Doador();
                    d.setIdUsuario(rs.getLong("id_usuario"));
                    d.setNome(rs.getString("nome"));
                    d.setEmail(rs.getString("email"));
                    d.setCpf(rs.getString("cpf"));
                    d.setTelefone(rs.getString("telefone"));
                    d.setDataCadastro(rs.getDate("data_cadastro").toLocalDate());
                    return d;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar doador por email", e);
        }
        return null;
    }
}