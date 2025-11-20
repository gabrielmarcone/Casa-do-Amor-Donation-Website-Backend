package com.casadoamor.service;

import java.time.LocalDate;
import com.casadoamor.enums.StatusInscricao;
import com.casadoamor.model.AreaAtuacao;
import com.casadoamor.model.Voluntario;
import com.casadoamor.dao.VoluntarioDAO;
import java.util.List;
import com.casadoamor.dao.AreaAtuacaoDAO;

public class VoluntarioService {

  private VoluntarioDAO voluntarioDAO;
  private AreaAtuacaoDAO areaAtuacaoDAO;

  public VoluntarioService(){
    this.voluntarioDAO = new VoluntarioDAO();
    this.areaAtuacaoDAO = new AreaAtuacaoDAO();
  }

  public void registrarInscricao(Voluntario voluntario) throws Exception{
    //feature 2.1

    //VALIDAÇÕES
    if (voluntario.getNome() == null || voluntario.getNome().trim().isEmpty()) {
      throw new Exception("O campo 'Nome' é obrigatório.");
    }
    if (voluntario.getEmail() == null || voluntario.getEmail().trim().isEmpty()) {
      throw new Exception("O campo 'Email' é obrigatório.");
    }
    if(voluntario.getCpf() == null || voluntario.getCpf().trim().isEmpty()){
      throw new Exception("O campo 'CPF' é obrigatório.");
    }
    if(voluntario.getTelefone() == null || voluntario.getTelefone().trim().isEmpty()){
      throw new Exception("O campo 'Telefone' é obrigatório.");
    }
    if(voluntario.getDataInscricao() == null){
      throw new Exception("O campo 'Data Inscrição' é obrigatório.");
    }

    // REGRAS DE NEGÓCIO
    voluntario.setStatusInscricao(StatusInscricao.PENDENTE_ANALISE); //RN-005

    // PERCISTÊNCIA
    try{
      voluntarioDAO.salvar(voluntario);
    } catch (Exception e){
      throw new Exception("Erro ao salvar inscrição no banco de dados: " + e.getMessage());
    }
    }

  public List<Voluntario> listarInscricoes(){
    //feature 2.2
    //VALIDAÇÕES

    //REGRAS DE NEGÓCIO

    //BUSCAR DADOS 
    return voluntarioDAO.listar();
  }

  public List<AreaAtuacao> buscarAreasParaDropdown(){
    return areaAtuacaoDAO.listar();
  }
}
