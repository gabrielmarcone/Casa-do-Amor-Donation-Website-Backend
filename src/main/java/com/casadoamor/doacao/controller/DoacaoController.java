package com.casadoamor.doacao.controller;

import java.util.List; // Import novo

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Import novo
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.casadoamor.doacao.dto.CriarDoacaoRequest;
import com.casadoamor.doacao.dto.CriarDoacaoResponse;
import com.casadoamor.doacao.model.Doacao;
import com.casadoamor.doacao.service.DoacaoService;

@RestController
@RequestMapping("/doacoes")
@CrossOrigin(origins = "*")
public class DoacaoController {

  @Autowired
  private DoacaoService doacaoService;

  // --- NOVO ENDPOINT (LISTAR TUDO) ---
  @GetMapping
  public ResponseEntity<List<Doacao>> listarDoacoes() {
      // Chama o Service -> DAO -> Banco de Dados
      List<Doacao> lista = doacaoService.listarTodas();
      return ResponseEntity.ok(lista);
  }
  // -----------------------------------

  @PostMapping
  public CriarDoacaoResponse criarDoacaoResponse(@RequestBody CriarDoacaoRequest request){
    return doacaoService.criarDoacao(request);
  }

  @GetMapping("/{id}")
  public Doacao buscarDoacao(@PathVariable Long id) {
    return doacaoService.buscarPorId(id);
  }

}