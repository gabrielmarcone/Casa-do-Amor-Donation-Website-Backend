package com.casadoamor.dao;

import com.casadoamor.enums.StatusInscricao;
import com.casadoamor.model.Voluntario;
import com.casadoamor.model.VoluntarioAreaAtuacao;
import com.casadoamor.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoluntarioDAO implements IVoluntarioDAO {

    @Override
    public void salvar(Voluntario voluntario) {
        // 1. Inserir na tabela Pai (Usuario)
        String sqlUsuario = "INSERT INTO usuario (nome, email, cpf, telefone) VALUES (?, ?, ?, ?)";
        // 2. Inserir na tabela Filha (Voluntario)
        String sqlVoluntario = "INSERT INTO voluntario (id_usuario, data_inscricao, status_inscricao) VALUES (?, ?, ?)";
        // 3. Inserir Áreas
        String sqlRelacao = "INSERT INTO voluntario_area_atuacao (id_usuario, id_area_atuacao, especialidade) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false); // Inicia Transação

            try {
                // Passo 1: Salvar Usuario e pegar ID
                Long idUsuarioGerado = null;
                try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, voluntario.getNome());
                    psUser.setString(2, voluntario.getEmail());
                    psUser.setString(3, voluntario.getCpf());
                    psUser.setString(4, voluntario.getTelefone());
                    psUser.executeUpdate();

                    try (ResultSet rs = psUser.getGeneratedKeys()) {
                        if (rs.next()) {
                            idUsuarioGerado = rs.getLong(1);
                            voluntario.setIdUsuario(idUsuarioGerado);
                        } else {
                            throw new SQLException("Falha ao obter ID do usuário.");
                        }
                    }
                }

                // Passo 2: Salvar Voluntario usando o mesmo ID
                try (PreparedStatement psVol = conn.prepareStatement(sqlVoluntario)) {
                    psVol.setLong(1, idUsuarioGerado);
                    psVol.setDate(2, Date.valueOf(voluntario.getDataInscricao()));
                    psVol.setString(3, voluntario.getStatusInscricao().name());
                    psVol.executeUpdate();
                }

                // Passo 3: Salvar Áreas de Atuação
                if (voluntario.getAreasDeAtuacao() != null && !voluntario.getAreasDeAtuacao().isEmpty()) {
                    try (PreparedStatement psRel = conn.prepareStatement(sqlRelacao)) {
                        for (VoluntarioAreaAtuacao rel : voluntario.getAreasDeAtuacao()) {
                            psRel.setLong(1, idUsuarioGerado); // Usa o ID do Usuario/Voluntario
                            psRel.setLong(2, rel.getAreaAtuacao().getIdArea());
                            psRel.setString(3, rel.getEspecialidade());
                            psRel.addBatch();
                        }
                        psRel.executeBatch();
                    }
                }

                conn.commit(); // Confirma tudo

            } catch (SQLException e) {
                conn.rollback(); // Desfaz se der erro
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar voluntário completo", e);
        }
    }

    @Override
    public List<Voluntario> listar() {
        // JOIN Obrigatório pois os dados estão espalhados
        String sql = "SELECT u.id_usuario, u.nome, u.email, u.cpf, u.telefone, v.data_inscricao, v.status_inscricao " +
                    "FROM voluntario v " +
                    "JOIN usuario u ON v.id_usuario = u.id_usuario";
        
        List<Voluntario> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Voluntario v = new Voluntario();
                v.setIdUsuario(rs.getLong("id_usuario"));
                v.setNome(rs.getString("nome"));
                v.setEmail(rs.getString("email"));
                v.setCpf(rs.getString("cpf"));
                v.setTelefone(rs.getString("telefone"));
                v.setDataInscricao(rs.getDate("data_inscricao").toLocalDate());
                v.setStatusInscricao(StatusInscricao.valueOf(rs.getString("status_inscricao")));
                lista.add(v);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar voluntários", e);
        }
        return lista;
    }

    // Novo Método Solicitado: Atualizar Status
    public void atualizarStatus(Long idUsuario, StatusInscricao novoStatus) {
        String sql = "UPDATE voluntario SET status_inscricao = ? WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoStatus.name());
            ps.setLong(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do voluntário", e);
        }
    }
}