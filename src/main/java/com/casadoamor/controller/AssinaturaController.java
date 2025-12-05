package com.casadoamor.controller;

import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.model.Assinatura;
import com.casadoamor.service.AssinaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assinaturas")
@CrossOrigin(origins = "*")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    public AssinaturaController() {
        this.assinaturaService = new AssinaturaService();
    }

    // Endpoint para criar nova assinatura (Sócio-Doador)
    @PostMapping
    public ResponseEntity<?> criarAssinatura(@RequestBody CriarDoacaoRequest request) {
        try {
            String linkAprovacao = assinaturaService.criarAssinatura(request);
            return ResponseEntity.ok().body("{\"link\": \"" + linkAprovacao + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar assinatura: " + e.getMessage());
        }
    }

    // Endpoint para Admin listar todos os sócios (Feature 1.4)
    @GetMapping("/admin/lista")
    public ResponseEntity<List<Assinatura>> listarAssinaturas() {
        List<Assinatura> lista = assinaturaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    // Endpoint para cancelar assinatura
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelarAssinatura(@PathVariable Long id) {
        try {
            assinaturaService.cancelarAssinatura(id);
            return ResponseEntity.ok("Assinatura cancelada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cancelar: " + e.getMessage());
        }
    }
}