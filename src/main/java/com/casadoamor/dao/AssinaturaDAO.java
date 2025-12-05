package com.casadoamor.dao;

import com.casadoamor.enums.StatusAssinatura;
import com.casadoamor.model.Assinatura;
import com.casadoamor.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Assinatura> listar() {
        // Busca os dados da assinatura E o nome do doador (fazendo JOIN com as tabelas de usuário)
        String sql = "SELECT a.*, u.nome as nome_doador " +
                    "FROM assinatura a " +
                    "JOIN doador d ON a.id_usuario_doador = d.id_usuario " +
                    "JOIN usuario u ON d.id_usuario = u.id_usuario " +
                    "ORDER BY a.data_inicio DESC";

        List<Assinatura> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Assinatura a = new Assinatura();
                a.setIdAssinatura(rs.getLong("id_assinatura"));
                a.setIdUsuarioDoador(rs.getLong("id_usuario_doador"));
                a.setStatusAssinatura(StatusAssinatura.valueOf(rs.getString("status_assinatura")));
                a.setDiaCobranca(rs.getInt("dia_cobranca"));
                a.setValorMensal(rs.getBigDecimal("valor_mensal"));
                a.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                a.setIdAssinaturaGateway(rs.getString("id_assinatura_gateway"));
                
                // NOTA: Se você quiser ver o nome no JSON, adicione o campo 'private String nomeDoador' 
                // na sua classe Assinatura.java e descomente a linha abaixo:
                // a.setNomeDoador(rs.getString("nome_doador")); 
                
                lista.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar assinaturas", e);
        }
        return lista;
    }

    public Assinatura buscarPorId(Long id) {
        String sql = "SELECT * FROM assinatura WHERE id_assinatura = ?";
        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Assinatura a = new Assinatura();
                    a.setIdAssinatura(rs.getLong("id_assinatura"));
                    a.setIdUsuarioDoador(rs.getLong("id_usuario_doador"));
                    a.setStatusAssinatura(StatusAssinatura.valueOf(rs.getString("status_assinatura")));
                    a.setDiaCobranca(rs.getInt("dia_cobranca"));
                    a.setValorMensal(rs.getBigDecimal("valor_mensal"));
                    a.setDataInicio(rs.getDate("data_inicio").toLocalDate());
                    a.setIdAssinaturaGateway(rs.getString("id_assinatura_gateway"));
                    return a;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar assinatura por ID", e);
        }
        return null;
    }

    public void atualizarStatus(Long idAssinatura, StatusAssinatura novoStatus) {
        String sql = "UPDATE assinatura SET status_assinatura = ? WHERE id_assinatura = ?";
        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, novoStatus.name());
            ps.setLong(2, idAssinatura);
            
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da assinatura", e);
        }
    }
}