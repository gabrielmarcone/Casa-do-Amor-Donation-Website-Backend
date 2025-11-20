package com.casadoamor.controller;

import com.casadoamor.model.AreaAtuacao;
import com.casadoamor.model.Voluntario;
import com.casadoamor.service.VoluntarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/voluntarios")
@CrossOrigin(origins = "*")
public class VoluntarioController {
  private final VoluntarioService voluntarioService;

  public VoluntarioController(){
    this.voluntarioService = new VoluntarioService();
  }

  @PostMapping("/inscricao")
  public ResponseEntity<String> receberInscricao(@RequestBody Voluntario voluntario){
    try{
      voluntarioService.registrarInscricao(voluntario);

      return ResponseEntity.ok("Inscrição realizada com sucesso! Aguarde nosso contato");
    } catch(Exception e){
      return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
    }
  }

  @GetMapping("/admin/lista")
    public ResponseEntity<List<Voluntario>> listarInscricoes() {
        // Busca a lista lá do banco (via Service -> DAO -> JDBC)
        List<Voluntario> lista = voluntarioService.listarInscricoes();
        
        // O Spring Boot converte automaticamente a List<Voluntario> para JSON aqui
        return ResponseEntity.ok(lista);
    }

  // Endpoint para carregar o dropdown
  @GetMapping("/areas-atuacao")
  public ResponseEntity<List<AreaAtuacao>> listarAreas() {
      List<AreaAtuacao> areas = voluntarioService.buscarAreasParaDropdown();
      return ResponseEntity.ok(areas);
  }
}
