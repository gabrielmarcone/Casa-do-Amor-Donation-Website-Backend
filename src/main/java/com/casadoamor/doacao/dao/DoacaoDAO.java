package com.casadoamor.doacao.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList; // Import necessário
import java.util.List;      // Import necessário

import com.casadoamor.doacao.enums.StatusDoacao;
import com.casadoamor.doacao.model.Doacao;
import com.casadoamor.util.ConnectionFactory;

public class DoacaoDAO {

    public DoacaoDAO() {
    }

    public void salvarDoacao(Doacao doacao) {
        String sql = "INSERT INTO Doacao (valor, moeda, statusDoacao, referenciaExt, idempotencyKey, nomeDoador, emailDoador, criadoEm, atualizadoEm, pagamentoId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setBigDecimal(1, doacao.getValor());
            pstm.setString(2, doacao.getMoeda());
            pstm.setString(3, doacao.getStatusDoacao() != null ? doacao.getStatusDoacao().name() : StatusDoacao.PENDING.name());
            pstm.setString(4, doacao.getReferenciaExt());
            pstm.setString(5, doacao.getIdempotencyKey());
            pstm.setString(6, doacao.getNomeDoador());
            pstm.setString(7, doacao.getEmailDoador());
            pstm.setTimestamp(8, doacao.getCriadoEm() != null ? Timestamp.valueOf(doacao.getCriadoEm()) : null);
            pstm.setTimestamp(9, doacao.getAtualizadoEm() != null ? Timestamp.valueOf(doacao.getAtualizadoEm()) : null);
            pstm.setString(10, doacao.getPagamentoId());

            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    doacao.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar doação no banco de dados", e);
        }
    }

    // --- NOVO MÉTODO PARA O FRONTADMIN ---
    public List<Doacao> listarTodas() {
        String sql = "SELECT * FROM Doacao ORDER BY criadoEm DESC";
        List<Doacao> lista = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar doações", e);
        }
        return lista;
    }
    // -------------------------------------

    public Doacao buscarPorId(Long id) {
        if (id == null) return null;
        
        String sql = "SELECT * FROM Doacao WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql)) {
            
            pstm.setLong(1, id);
            
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar doação por ID", e);
        }
        return null;
    }

    public Doacao buscaPorRefEx(String referenciaExterna) {
        if (referenciaExterna == null) return null;

        String sql = "SELECT * FROM Doacao WHERE referenciaExt = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql)) {
            
            pstm.setString(1, referenciaExterna);
            
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar doação por referência externa", e);
        }
        return null;
    }

    public void atualizarDoacao(Doacao doacao) {
        if (doacao == null || doacao.getId() == null) return;

        String sql = "UPDATE Doacao SET statusDoacao = ?, pagamentoId = ?, atualizadoEm = ? WHERE id = ?";

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql)) {

            pstm.setString(1, doacao.getStatusDoacao().name());
            pstm.setString(2, doacao.getPagamentoId());
            pstm.setTimestamp(3, doacao.getAtualizadoEm() != null ? Timestamp.valueOf(doacao.getAtualizadoEm()) : null);
            pstm.setLong(4, doacao.getId());

            pstm.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar doação", e);
        }
    }

    public void atualizarStatusDoacao(Long id, StatusDoacao status) {
        if (id == null || status == null) return;

        String sql = "UPDATE Doacao SET statusDoacao = ? WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql)) {
            
            pstm.setString(1, status.name());
            pstm.setLong(2, id);
            
            pstm.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da doação", e);
        }
    }

    private Doacao mapearResultSet(ResultSet rs) throws SQLException {
        Doacao d = new Doacao();
        d.setId(rs.getLong("id"));
        d.setValor(rs.getBigDecimal("valor"));
        d.setMoeda(rs.getString("moeda"));
        d.setReferenciaExt(rs.getString("referenciaExt"));
        d.setIdempotencyKey(rs.getString("idempotencyKey"));
        d.setPagamentoId(rs.getString("pagamentoId"));
        d.setNomeDoador(rs.getString("nomeDoador"));
        d.setEmailDoador(rs.getString("emailDoador"));
        
        String statusStr = rs.getString("statusDoacao");
        if (statusStr != null) {
            d.setStatusDoacao(StatusDoacao.valueOf(statusStr));
        }

        Timestamp criadoEm = rs.getTimestamp("criadoEm");
        if (criadoEm != null) {
            d.setCriadoEm(criadoEm.toLocalDateTime());
        }

        Timestamp atualizadoEm = rs.getTimestamp("atualizadoEm");
        if (atualizadoEm != null) {
            d.setAtualizadoEm(atualizadoEm.toLocalDateTime());
        }

        return d;
    }
}