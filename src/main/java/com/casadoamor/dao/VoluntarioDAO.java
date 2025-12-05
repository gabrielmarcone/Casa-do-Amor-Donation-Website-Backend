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
}