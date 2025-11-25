package com.casadoamor.doacao.dao;

import com.casadoamor.doacao.model.Doacao;
import com.casadoamor.util.ConnectionFactory;
import com.casadoamor.doacao.enums.StatusDoacao;

public class DoacaoDAO {

  public DoacaoDAO() {
  }

  public void salvarDoacao(Doacao doacao) {
    // implementar SQL
  }

  public Doacao buscarPorId(Long id) {
    // implementar SQL
    return null;
  }

  public Doacao buscaPorRefEx(String referenciaExterna){
    // implementar SQL
    return null;
  }

  public void atualizarDoacao(Doacao doacao) {
    // implementar SQL
  }

  public void atualizarStatusDoacao(Long id, StatusDoacao status) {
    // implementar SQL
  }


}
