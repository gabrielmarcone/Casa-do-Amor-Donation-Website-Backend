package com.casadoamor.dao;

import java.sql.Connection;
import com.casadoamor.model.Voluntario;
import com.casadoamor.util.ConnectionFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casadoamor.enums.StatusInscricao;
import java.sql.Date;


public class VoluntarioDAO implements IVoluntarioDAO{

  public VoluntarioDAO(){

  }

  @Override
  public void salvar(Voluntario voluntario) {
    String sql = "INSERT INTO Voluntario (idUsuario, nome, email, cpf, telefone, dataInscricao, statusInscricao) VALUES(?, ?, ?, ?, ?, ?, ?)";
    
    try(Connection connection = ConnectionFactory.getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql)) {
      pstm.setString(1, voluntario.getNome());
      pstm.setString(2, voluntario.getEmail());
      pstm.setString(3, voluntario.getCpf());
      pstm.setString(4, voluntario.getTelefone());
      pstm.setDate(5, Date.valueOf(voluntario.getDataInscricao()));
      pstm.setString(6, voluntario.getStatusInscricao().toString());

      pstm.execute();
    }catch (Exception e){
      e.printStackTrace();

      throw new RuntimeException("Erro ao salvar voluntario", e);
    }
  }

  @Override
  public List<Voluntario> listar() { 
    /* So funciona se a tabela 'Voluntario' tambem tiver as colunas nome, email, etc.
     * se as tabelas usuario e voluntario forem separadas (como no MER), o SQL correto sera um JOIN
     * String sql = "SELECT * FROM Voluntario v JOIN Usuario u ON v.id_usuario = u.id_usuario";
      */
    String sql = "SELECT * FROM Voluntario";
    List<Voluntario> retorno = new ArrayList<>();
    try(Connection connection = ConnectionFactory.getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet resultado = pstm.executeQuery()) {
      
      while(resultado.next()){
        Voluntario voluntario = new Voluntario();
        voluntario.setIdUsuario(resultado.getLong("idUsuario"));
        voluntario.setNome(resultado.getString("nome"));
        voluntario.setEmail(resultado.getString("email"));
        voluntario.setCpf(resultado.getString("cpf"));
        voluntario.setTelefone(resultado.getString("telefone"));
        Date dataDoBanco = resultado.getDate("dataInscricao");
        // Conversao de tipos necessarias abaixo
        if(dataDoBanco != null){
          voluntario.setDataInscricao(dataDoBanco.toLocalDate()); 
        }
        String statusDoBanco = resultado.getString("statusInscricao");
        if(statusDoBanco != null){
          voluntario.setStatusInscricao(StatusInscricao.valueOf(statusDoBanco));
        }
        retorno.add(voluntario);
      }
    } catch (SQLException ex){
      Logger.getLogger(VoluntarioDAO.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException("Erro ao listar voluntarios", ex);
    }
    return retorno;
  }

}
