package com.casadoamor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.casadoamor.model.Doacao; // Lembre-se que movemos o Model pra cá
import com.casadoamor.enums.StatusDoacao; // E o Enum pra cá
import com.casadoamor.util.ConnectionFactory;

public class DoacaoDAO {

    public DoacaoDAO() {
    }

    public void salvarDoacao(Doacao doacao) {
        String sql = "INSERT INTO doacao (valor, moeda, status_doacao, referencia_ext, idempotency_key, nome_doador, email_doador, criado_em, atualizado_em) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
        doacao.setPagamentoId(rs.getString("pagamento_id"));
        doacao.setIdempotencyKey(rs.getString("idempotency_key"));
        doacao.setNomeDoador(rs.getString("nome_doador"));
        doacao.setEmailDoador(rs.getString("email_doador"));

        // Converte Timestamp do banco de volta para LocalDateTime
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        if (criadoEm != null) doacao.setCriadoEm(criadoEm.toLocalDateTime());

        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em");
        if (atualizadoEm != null) doacao.setAtualizadoEm(atualizadoEm.toLocalDateTime());

        return doacao;
    }

    public List<Doacao> listarTodas() {
        // Busca doacao + dados do pagamento + nome do doador (caso esteja na tabela doacao)
        // Se a tabela doacao tiver FK para usuario, faça o JOIN com usuario. 
        // Como no seu código atual 'nome_doador' é uma coluna texto na tabela doacao, faremos SELECT direto.
        
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