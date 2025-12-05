package com.casadoamor.dao;

import java.util.List;
import com.casadoamor.model.Voluntario;

public interface IVoluntarioDAO {
    void salvar(Voluntario voluntario);
    List<Voluntario> listar();
    void atualizar(Voluntario voluntario); // NOVO
    void excluir(Long idUsuario);          // NOVO
    Voluntario buscarPorId(Long id);       // NOVO (Necessário para verificar antes de update/delete)
}