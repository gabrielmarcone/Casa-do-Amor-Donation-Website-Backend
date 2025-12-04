package com.casadoamor.controller;

import com.casadoamor.model.AreaAtuacao;
import com.casadoamor.model.Voluntario;
import com.casadoamor.service.VoluntarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/voluntarios")
@CrossOrigin(origins = "*")
public class VoluntarioController {

    private final VoluntarioService voluntarioService;

    public VoluntarioController() {
        this.voluntarioService = new VoluntarioService();
    }

    @PostMapping("/inscricao")
    public ResponseEntity<String> receberInscricao(@RequestBody Voluntario voluntario) {
        try {
            voluntarioService.registrarInscricao(voluntario);
            return ResponseEntity.ok("Inscrição realizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/admin/lista")
    public ResponseEntity<List<Voluntario>> listarInscricoes() {
        // Num cenário real, verificar se o usuário logado é ADMIN aqui
        return ResponseEntity.ok(voluntarioService.listarInscricoes());
    }

    @GetMapping("/areas-atuacao")
    public ResponseEntity<List<AreaAtuacao>> listarAreas() {
        return ResponseEntity.ok(voluntarioService.buscarAreasParaDropdown());
    }

    // Novo Endpoint: Avaliação (Aprovado/Reprovado)
    @PatchMapping("/admin/{id}/avaliacao")
    public ResponseEntity<String> avaliarCandidato(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean aprovado = payload.get("aprovado");
            if (aprovado == null) return ResponseEntity.badRequest().body("Campo 'aprovado' é obrigatório");
            
            voluntarioService.avaliarCandidato(id, aprovado);
            return ResponseEntity.ok("Status do voluntário atualizado para " + (aprovado ? "APROVADO" : "REJEITADO"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao avaliar: " + e.getMessage());
        }
    }
}