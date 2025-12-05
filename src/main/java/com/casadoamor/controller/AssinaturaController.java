package com.casadoamor.controller;

import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.enums.StatusAssinatura;
import com.casadoamor.model.Assinatura;
import com.casadoamor.service.AssinaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas")
@CrossOrigin(origins = "*")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    public AssinaturaController() {
        this.assinaturaService = new AssinaturaService();
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> criarAssinatura(@RequestBody CriarDoacaoRequest request) {
        try {
            String linkAprovacao = assinaturaService.criarAssinatura(request);
            return ResponseEntity.ok().body("{\"link\": \"" + linkAprovacao + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar assinatura: " + e.getMessage());
        }
    }

    // READ (Admin)
    @GetMapping("/admin/lista")
    public ResponseEntity<List<Assinatura>> listarAssinaturas() {
        return ResponseEntity.ok(assinaturaService.listarTodas());
    }

    // DELETE (Lógico/Anular) - Serve para Admin e Usuário
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelarAssinatura(@PathVariable Long id) {
        try {
            assinaturaService.cancelarAssinatura(id);
            return ResponseEntity.ok("Assinatura anulada/cancelada com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cancelar: " + e.getMessage());
        }
    }

    // UPDATE (Admin - NOVO!) - Mudar status manualmente
    // Exemplo JSON: { "status": "INADIMPLENTE" }
    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<String> alterarStatusManual(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String statusStr = payload.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest().body("Status é obrigatório");
            }
            
            StatusAssinatura novoStatus = StatusAssinatura.valueOf(statusStr.toUpperCase());
            assinaturaService.alterarStatusManual(id, novoStatus);
            
            return ResponseEntity.ok("Status da assinatura atualizado para " + novoStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Status inválido. Use: ATIVA, INADIMPLENTE ou CANCELADA");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar status: " + e.getMessage());
        }
    }
}