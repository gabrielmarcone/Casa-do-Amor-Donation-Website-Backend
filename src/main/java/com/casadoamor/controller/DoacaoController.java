package com.casadoamor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.dto.CriarDoacaoResponse;
import com.casadoamor.doacao.service.DoacaoService;

import com.casadoamor.model.Doacao;;


@RestController
@RequestMapping("/doacoes")
@CrossOrigin(origins = "*")
public class DoacaoController {

  @Autowired
  private DoacaoService doacaoService;

  @PostMapping
  public CriarDoacaoResponse criarDoacaoResponse(@RequestBody CriarDoacaoRequest request){
    return doacaoService.criarDoacao(request);
  }

  @GetMapping("/{id}")
  public Doacao buscarDoacao(@PathVariable Long id) {
    return doacaoService.buscarPorId(id);
  }

}
