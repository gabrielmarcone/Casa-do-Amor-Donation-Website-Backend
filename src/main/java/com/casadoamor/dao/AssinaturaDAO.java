package com.casadoamor.dao;

import com.casadoamor.enums.StatusAssinatura;
import com.casadoamor.model.Assinatura;
import com.casadoamor.util.ConnectionFactory;

import java.sql.*;

public class AssinaturaDAO {

    public void salvar(Assinatura assinatura) {
        String sql = "INSERT INTO assinatura (id_usuario_doador, status_assinatura, dia_cobranca, valor_mensal, data_inicio, id_assinatura_gateway) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, assinatura.getIdUsuarioDoador());
            ps.setString(2, assinatura.getStatusAssinatura().name());
            ps.setInt(3, assinatura.getDiaCobranca());
            ps.setBigDecimal(4, assinatura.getValorMensal());
            ps.setDate(5, Date.valueOf(assinatura.getDataInicio()));
            ps.setString(6, assinatura.getIdAssinaturaGateway());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    assinatura.setIdAssinatura(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar assinatura", e);
        }
    }
    
    // Método para o Admin listar assinaturas (Feature 1.4)
    // Implementar listar() se necessário...
}