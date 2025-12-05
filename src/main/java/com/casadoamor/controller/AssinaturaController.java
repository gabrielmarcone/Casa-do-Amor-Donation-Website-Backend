package com.casadoamor.controller;

import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.service.AssinaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assinaturas")
@CrossOrigin(origins = "*")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    public AssinaturaController() {
        this.assinaturaService = new AssinaturaService();
    }

    @PostMapping
    public ResponseEntity<?> criarAssinatura(@RequestBody CriarDoacaoRequest request) {
        try {
            // Retorna a URL de aprovação (init_point) ou ID da assinatura
            String linkAprovacao = assinaturaService.criarAssinatura(request);
            return ResponseEntity.ok().body("{\"link\": \"" + linkAprovacao + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar assinatura: " + e.getMessage());
        }
    }
}