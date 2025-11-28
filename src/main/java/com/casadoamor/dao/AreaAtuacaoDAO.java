package com.casadoamor.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.casadoamor.model.AreaAtuacao;
import com.casadoamor.util.ConnectionFactory;

public class AreaAtuacaoDAO {
  // Método essencial para o dropdown do formulário
  public List<AreaAtuacao> listar() {
    String sql = "SELECT * FROM AreaAtuacao";
    List<AreaAtuacao> retorno = new ArrayList<>();
    try(Connection connection = ConnectionFactory.getConnection();
        PreparedStatement pstm = connection.prepareStatement(sql);
        ResultSet resultado = pstm.executeQuery()) {

          while(resultado.next()){
            AreaAtuacao area = new AreaAtuacao();
            area.setIdArea(resultado.getLong("idArea"));
            area.setNome(resultado.getString("nome"));
            retorno.add(area);
          }
        } catch (SQLException ex){
      Logger.getLogger(VoluntarioDAO.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException("Erro ao listar areas de atuação", ex);
    }
    return retorno;
  }
}
