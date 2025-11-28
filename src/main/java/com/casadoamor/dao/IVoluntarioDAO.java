package com.casadoamor.dao;

import java.util.List;
import com.casadoamor.model.Voluntario;

public interface IVoluntarioDAO {
  void salvar(Voluntario voluntario); // Para a feature 2.1
  List<Voluntario> listar(); // Para a feature 2.2
}
