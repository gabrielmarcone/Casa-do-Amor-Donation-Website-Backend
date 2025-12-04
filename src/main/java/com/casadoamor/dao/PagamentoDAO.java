package com.casadoamor.dao;

import com.casadoamor.enums.MetodoPagamento;
import com.casadoamor.enums.StatusPagamento;
import com.casadoamor.model.Pagamento;
import com.casadoamor.util.ConnectionFactory;

import java.sql.*;

public class PagamentoDAO {

    public Pagamento salvar(Pagamento pagamento) {
        String sql = "INSERT INTO pagamento (valor, metodo_pagamento, data_pagamento, status_pagamento, gateway_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBigDecimal(1, pagamento.getValor());
            ps.setString(2, pagamento.getMetodoPagamento().name());
            // Se dataPagamento for null, usa current timestamp
            ps.setTimestamp(3, pagamento.getDataPagamento() != null ? Timestamp.valueOf(pagamento.getDataPagamento()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(4, pagamento.getStatusPagamento().name());
            ps.setString(5, pagamento.getGatewayId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pagamento.setIdPagamento(rs.getLong(1));
                }
            }
            return pagamento;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pagamento", e);
        }
    }

    public void atualizarStatus(Long idPagamento, StatusPagamento status, String gatewayId) {
        String sql = "UPDATE pagamento SET status_pagamento = ?, gateway_id = ? WHERE id_pagamento = ?";
        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, gatewayId);
            ps.setLong(3, idPagamento);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status pagamento", e);
        }
    }
}