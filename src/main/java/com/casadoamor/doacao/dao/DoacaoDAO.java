package com.casadoamor.doacao.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.casadoamor.doacao.enums.StatusDoacao;
import com.casadoamor.doacao.model.Doacao;

public class DoacaoDAO {

  private static final AtomicLong SEQUENCE = new AtomicLong(1);
  private static final Map<Long, Doacao> DOACOES = new ConcurrentHashMap<>();
  private static final Map<String, Long> REFERENCIAS = new ConcurrentHashMap<>();

  public DoacaoDAO() {
  }

  public void salvarDoacao(Doacao doacao) {
    long id = SEQUENCE.getAndIncrement();
    doacao.setId(id);
    DOACOES.put(id, doacao);
    if (doacao.getReferenciaExt() != null) {
      REFERENCIAS.put(doacao.getReferenciaExt(), id);
    }
  }

  public Doacao buscarPorId(Long id) {
    if (id == null) {
      return null;
    }
    return DOACOES.get(id);
  }

  public Doacao buscaPorRefEx(String referenciaExterna) {
    if (referenciaExterna == null) {
      return null;
    }
    Long id = REFERENCIAS.get(referenciaExterna);
    if (id == null) {
      return null;
    }
    return DOACOES.get(id);
  }

  public void atualizarDoacao(Doacao doacao) {
    if (doacao == null || doacao.getId() == null) {
      return;
    }
    DOACOES.put(doacao.getId(), doacao);
    if (doacao.getReferenciaExt() != null) {
      REFERENCIAS.put(doacao.getReferenciaExt(), doacao.getId());
    }
  }

  public void atualizarStatusDoacao(Long id, StatusDoacao status) {
    if (id == null || status == null) {
      return;
    }
    Doacao doacao = DOACOES.get(id);
    if (doacao != null) {
      doacao.setStatusDoacao(status);
      DOACOES.put(id, doacao);
    }
  }

}
