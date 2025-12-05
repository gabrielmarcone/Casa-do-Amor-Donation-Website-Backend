package com.casadoamor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Import necessário para RETURN_GENERATED_KEYS
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casadoamor.model.Voluntario;
import com.casadoamor.model.VoluntarioAreaAtuacao;
import com.casadoamor.util.ConnectionFactory;
import com.casadoamor.enums.StatusInscricao;

public class VoluntarioDAO implements IVoluntarioDAO {

    public VoluntarioDAO() {
    }

    @Override
    public void salvar(Voluntario voluntario) {
        // SQL para inserir na tabela principal
        String sqlVoluntario = "INSERT INTO Voluntario (nome, email, cpf, telefone, dataInscricao, statusInscricao) VALUES(?, ?, ?, ?, ?, ?)";
        // SQL para inserir na tabela de relacionamento (Muitos-para-Muitos)
        String sqlRelacao = "INSERT INTO voluntario_area_atuacao (id_voluntario, id_area_atuacao, especialidade) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionFactory.getConnection()) {
            // Desativa o auto-commit para garantir que tudo seja salvo ou nada seja salvo (Transação Atômica)
            connection.setAutoCommit(false);
            
            Long idVoluntarioGerado = null;

            // 1. Salva o Voluntário e solicita ao driver o ID gerado (RETURN_GENERATED_KEYS)
            try (PreparedStatement pstm = connection.prepareStatement(sqlVoluntario, Statement.RETURN_GENERATED_KEYS)) {
                pstm.setString(1, voluntario.getNome());
                pstm.setString(2, voluntario.getEmail());
                pstm.setString(3, voluntario.getCpf());
                pstm.setString(4, voluntario.getTelefone());
                pstm.setDate(5, Date.valueOf(voluntario.getDataInscricao()));
                pstm.setString(6, voluntario.getStatusInscricao().toString());

                // Executa a inserção
                int linhasAfetadas = pstm.executeUpdate();

                // Se inseriu com sucesso, busca a chave primária gerada
                if (linhasAfetadas > 0) {
                    try (ResultSet rs = pstm.getGeneratedKeys()) {
                        if (rs.next()) {
                            idVoluntarioGerado = rs.getLong(1);
                        }
                    }
                }
            }

            // Verifica se o ID foi recuperado. Se não, lança erro para abortar a transação.
            if (idVoluntarioGerado == null) {
                throw new SQLException("Falha ao salvar voluntário: ID não foi gerado.");
            }

            // 2. Salva as Áreas de Atuação e Especialidades (se houver)
            if (voluntario.getAreasDeAtuacao() != null && !voluntario.getAreasDeAtuacao().isEmpty()) {
                try (PreparedStatement pstmRel = connection.prepareStatement(sqlRelacao)) {
                    for (VoluntarioAreaAtuacao relacao : voluntario.getAreasDeAtuacao()) {
                        pstmRel.setLong(1, idVoluntarioGerado); // Usa o ID recuperado acima
                        pstmRel.setLong(2, relacao.getAreaAtuacao().getIdArea());
                        pstmRel.setString(3, relacao.getEspecialidade());
                        
                        pstmRel.addBatch(); // Adiciona ao lote para execução em massa
                    }
                    pstmRel.executeBatch(); // Executa todos os inserts de relacionamento
                }
            }

            // Se chegou até aqui sem erros, confirma a transação no banco
            connection.commit();
            
        } catch (Exception e) {
            // O rollback é automático ao fechar a conexão sem commit, mas o log ajuda no debug
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar voluntário completo: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Voluntario> listar() {
        // Seleciona todos os dados da tabela Voluntario
        String sql = "SELECT * FROM Voluntario";
        List<Voluntario> retorno = new ArrayList<>();
        
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement pstm = connection.prepareStatement(sql);
             ResultSet resultado = pstm.executeQuery()) {

            while (resultado.next()) {
                Voluntario voluntario = new Voluntario();
                
                // Mapeamento das colunas do ResultSet para o Objeto Java
                voluntario.setIdUsuario(resultado.getLong("idUsuario"));
                voluntario.setNome(resultado.getString("nome"));
                voluntario.setEmail(resultado.getString("email"));
                voluntario.setCpf(resultado.getString("cpf"));
                voluntario.setTelefone(resultado.getString("telefone"));
                
                // Conversão segura de Date SQL para LocalDate Java
                Date dataDoBanco = resultado.getDate("dataInscricao");
                if (dataDoBanco != null) {
                    voluntario.setDataInscricao(dataDoBanco.toLocalDate());
                }
                
                // Conversão de String para Enum
                String statusDoBanco = resultado.getString("statusInscricao");
                if (statusDoBanco != null) {
                    voluntario.setStatusInscricao(StatusInscricao.valueOf(statusDoBanco));
                }
                
                retorno.add(voluntario);
            }
        } catch (SQLException ex) {
            Logger.getLogger(VoluntarioDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Erro ao listar voluntarios", ex);
        }
        return retorno;
    }

    @Override
    public Voluntario buscarPorId(Long id) {
        String sql = "SELECT * FROM Voluntario WHERE idUsuario = ?"; // Ou JOIN com Usuario se precisar de todos os campos
        // Implementação simplificada para checagem de existência
        // ... (Seu código de busca aqui, similar ao listar mas com WHERE)
        return null; // Placeholder se não encontrar
    }

    @Override
    public void atualizar(Voluntario voluntario) {
        String sqlUsuario = "UPDATE Usuario SET nome=?, email=?, cpf=?, telefone=? WHERE idUsuario=?";
        String sqlVoluntario = "UPDATE Voluntario SET dataInscricao=?, statusInscricao=? WHERE id_usuario=?";
        // Para áreas de atuação, a estratégia mais simples é DELETE seguido de INSERT
        String deleteAreas = "DELETE FROM voluntario_area_atuacao WHERE id_voluntario=?";
        String insertAreas = "INSERT INTO voluntario_area_atuacao (id_voluntario, id_area_atuacao, especialidade) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Atualiza Usuario
                try (PreparedStatement ps = conn.prepareStatement(sqlUsuario)) {
                    ps.setString(1, voluntario.getNome());
                    ps.setString(2, voluntario.getEmail());
                    ps.setString(3, voluntario.getCpf());
                    ps.setString(4, voluntario.getTelefone());
                    ps.setLong(5, voluntario.getIdUsuario());
                    ps.executeUpdate();
                }
                // 2. Atualiza Voluntario
                try (PreparedStatement ps = conn.prepareStatement(sqlVoluntario)) {
                    ps.setDate(1, Date.valueOf(voluntario.getDataInscricao()));
                    ps.setString(2, voluntario.getStatusInscricao().toString());
                    ps.setLong(3, voluntario.getIdUsuario());
                    ps.executeUpdate();
                }
                // 3. Atualiza Áreas (Remove antigas e põe novas)
                try (PreparedStatement ps = conn.prepareStatement(deleteAreas)) {
                    ps.setLong(1, voluntario.getIdUsuario());
                    ps.executeUpdate();
                }
                if (voluntario.getAreasDeAtuacao() != null) {
                    try (PreparedStatement ps = conn.prepareStatement(insertAreas)) {
                        for (VoluntarioAreaAtuacao area : voluntario.getAreasDeAtuacao()) {
                            ps.setLong(1, voluntario.getIdUsuario());
                            ps.setLong(2, area.getAreaAtuacao().getIdArea());
                            ps.setString(3, area.getEspecialidade());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar voluntário", e);
        }
    }

    @Override
    public void excluir(Long idUsuario) {
        String deleteAreas = "DELETE FROM voluntario_area_atuacao WHERE id_voluntario=?";
        String deleteVoluntario = "DELETE FROM Voluntario WHERE id_usuario=?";
        String deleteUsuario = "DELETE FROM Usuario WHERE idUsuario=?"; // Cuidado com integridade referencial

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Remove dependências (Áreas)
                try (PreparedStatement ps = conn.prepareStatement(deleteAreas)) {
                    ps.setLong(1, idUsuario);
                    ps.executeUpdate();
                }
                // 2. Remove Filho (Voluntario)
                try (PreparedStatement ps = conn.prepareStatement(deleteVoluntario)) {
                    ps.setLong(1, idUsuario);
                    ps.executeUpdate();
                }
                // 3. Remove Pai (Usuario)
                try (PreparedStatement ps = conn.prepareStatement(deleteUsuario)) {
                    ps.setLong(1, idUsuario);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir voluntário", e);
        }
    }
}