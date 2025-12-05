package com.casadoamor.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.casadoamor.model.Doacao; 
import com.casadoamor.enums.StatusDoacao; 
import com.casadoamor.util.ConnectionFactory;

public class DoacaoDAO {

    public DoacaoDAO() {
    }

    public void salvarDoacao(Doacao doacao) {
        // ATUALIZADO: Adicionadas as colunas 'id_assinatura' e 'pagamento_id'
        String sql = "INSERT INTO doacao (valor, moeda, status_doacao, referencia_ext, idempotency_key, nome_doador, email_doador, criado_em, atualizado_em, id_assinatura, pagamento_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             // O flag RETURN_GENERATED_KEYS é essencial para pegar o ID criado pelo banco
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBigDecimal(1, doacao.getValor());
            ps.setString(2, doacao.getMoeda());
            ps.setString(3, doacao.getStatusDoacao().name()); // Salva o ENUM como texto
            ps.setString(4, doacao.getReferenciaExt());
            ps.setString(5, doacao.getIdempotencyKey());
            ps.setString(6, doacao.getNomeDoador());
            ps.setString(7, doacao.getEmailDoador());
            
            // Conversão de LocalDateTime para Timestamp do SQL
            ps.setTimestamp(8, Timestamp.valueOf(doacao.getCriadoEm())); 
            ps.setTimestamp(9, Timestamp.valueOf(doacao.getAtualizadoEm()));

            // --- NOVO: Tratamento para campo id_assinatura (pode ser NULL) ---
            if (doacao.getIdAssinatura() != null) {
                ps.setLong(10, doacao.getIdAssinatura());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            // --- NOVO: Salva o ID do pagamento vinculado ---
            ps.setString(11, doacao.getPagamentoId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        doacao.setId(rs.getLong(1)); // Define o ID gerado no objeto
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar doação", e);
        }
    }

    public Doacao buscarPorId(Long id) {
        String sql = "SELECT * FROM doacao WHERE id = ?";
        Doacao doacao = null;

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doacao = mapearDoacao(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar doação por ID", e);
        }
        return doacao;
    }

    public Doacao buscaPorRefEx(String referenciaExterna) {
        String sql = "SELECT * FROM doacao WHERE referencia_ext = ?";
        Doacao doacao = null;

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenciaExterna);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doacao = mapearDoacao(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar doação por referência externa", e);
        }
        return doacao;
    }

    public void atualizarDoacao(Doacao doacao) {
        // Atualiza campos que podem mudar após o callback do pagamento (ex: status e pagamento_id)
        String sql = "UPDATE doacao SET status_doacao = ?, pagamento_id = ?, atualizado_em = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doacao.getStatusDoacao().name());
            ps.setString(2, doacao.getPagamentoId());
            ps.setTimestamp(3, Timestamp.valueOf(doacao.getAtualizadoEm()));
            ps.setLong(4, doacao.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar doação", e);
        }
    }

    public void atualizarStatusDoacao(Long id, StatusDoacao status) {
        String sql = "UPDATE doacao SET status_doacao = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setLong(2, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status da doação", e);
        }
    }

    // Método auxiliar para converter o ResultSet em Objeto (evita repetição de código)
    private Doacao mapearDoacao(ResultSet rs) throws SQLException {
        Doacao doacao = new Doacao();
        doacao.setId(rs.getLong("id"));
        doacao.setValor(rs.getBigDecimal("valor"));
        doacao.setMoeda(rs.getString("moeda"));
        
        // Converte String do banco de volta para Enum
        String statusStr = rs.getString("status_doacao");
        if (statusStr != null) {
            doacao.setStatusDoacao(StatusDoacao.valueOf(statusStr));
        }

        doacao.setReferenciaExt(rs.getString("referencia_ext"));
        doacao.setPagamentoId(rs.getString("pagamento_id")); // Agora lê corretamente do banco
        doacao.setIdempotencyKey(rs.getString("idempotency_key"));
        doacao.setNomeDoador(rs.getString("nome_doador"));
        doacao.setEmailDoador(rs.getString("email_doador"));

        // Converte Timestamp do banco de volta para LocalDateTime
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) doacao.setCriadoEm(criadoEm.toLocalDateTime());

        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) doacao.setAtualizadoEm(atualizadoEm.toLocalDateTime());

        // --- NOVO: Lê o ID da Assinatura ---
        long idAssinatura = rs.getLong("id_assinatura");
        // Verifica se a última coluna lida era NULL (pois 0 pode ser um valor válido para long primitivo, mas aqui indica null)
        if (!rs.wasNull()) {
            doacao.setIdAssinatura(idAssinatura);
        }

        return doacao;
    }

    public List<Doacao> listarTodas() {
        String sql = "SELECT * FROM doacao ORDER BY criado_em DESC";
        List<Doacao> lista = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearDoacao(rs)); // Reutiliza seu método auxiliar existente
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todas as doações", e);
        }
        return lista;
    }
}