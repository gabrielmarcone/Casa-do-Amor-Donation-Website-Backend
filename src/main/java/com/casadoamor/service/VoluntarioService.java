package com.casadoamor.service;

import com.casadoamor.dao.AreaAtuacaoDAO;
import com.casadoamor.dao.VoluntarioDAO;
import com.casadoamor.enums.StatusInscricao;
import com.casadoamor.model.AreaAtuacao;
import com.casadoamor.model.Voluntario;

import java.time.LocalDate;
import java.util.List;

public class VoluntarioService {

    private final VoluntarioDAO voluntarioDAO;
    private final AreaAtuacaoDAO areaAtuacaoDAO;

    public VoluntarioService() {
        this.voluntarioDAO = new VoluntarioDAO();
        this.areaAtuacaoDAO = new AreaAtuacaoDAO();
    }

    public void registrarInscricao(Voluntario voluntario) throws Exception {
        if (voluntario.getNome() == null || voluntario.getNome().trim().isEmpty()) throw new Exception("Nome obrigatório.");
        if (voluntario.getEmail() == null || voluntario.getEmail().trim().isEmpty()) throw new Exception("Email obrigatório.");
        
        voluntario.setDataInscricao(LocalDate.now());
        voluntario.setStatusInscricao(StatusInscricao.PENDENTE_ANALISE); // RN-005

        voluntarioDAO.salvar(voluntario);
    }

    public List<Voluntario> listarInscricoes() {
        return voluntarioDAO.listar();
    }

    public List<AreaAtuacao> buscarAreasParaDropdown() {
        return areaAtuacaoDAO.listar();
    }

    // Novo Método: Avaliar Candidato
    public void avaliarCandidato(Long idUsuario, boolean aprovado) {
        StatusInscricao novoStatus = aprovado ? StatusInscricao.APROVADA : StatusInscricao.REJEITADA;
        voluntarioDAO.atualizarStatus(idUsuario, novoStatus);
        
        // Opcional: Aqui você poderia enviar um email notificando o voluntário
        // emailService.enviarNotificacao(idUsuario, novoStatus);
    }
}