package com.casadoamor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.casadoamor.dto.CriarDoacaoRequest;
import com.casadoamor.dto.CriarDoacaoResponse;
import com.casadoamor.service.DoacaoService; // Atenção: Mova o Service para este pacote se ainda não moveu
import com.casadoamor.model.Doacao;

import java.util.List;

@RestController
@RequestMapping("/doacoes")
@CrossOrigin(origins = "*")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    // Endpoint público para criar doação (Pix/Cartão)
    @PostMapping
    public ResponseEntity<CriarDoacaoResponse> criarDoacaoResponse(@RequestBody CriarDoacaoRequest request) {
        try {
            CriarDoacaoResponse response = doacaoService.criarDoacao(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Em produção, evite expor o erro exato, mas para dev ajuda
            return ResponseEntity.badRequest().body(new CriarDoacaoResponse()); 
        }
    }

    // Busca pública por ID (útil para tela de "Obrigado" ou confirmação)
    @GetMapping("/{id}")
    public ResponseEntity<Doacao> buscarDoacao(@PathVariable Long id) {
        Doacao doacao = doacaoService.buscarPorId(id);
        if (doacao != null) {
            return ResponseEntity.ok(doacao);
        }
        return ResponseEntity.notFound().build();
    }

    // --- ÁREA ADMINISTRATIVA ---
    
    // Feature 1.3 - Gestão de Doações (Admin)
    // Este endpoint deve listar TODAS as doações para o controle financeiro
    @GetMapping("/admin/lista")
    public ResponseEntity<List<Doacao>> listarTodas() {
        // Lembre-se de adicionar o método 'listarTodas()' no seu DoacaoService
        // que por sua vez chama o 'doacaoDAO.listarTodas()' que você já implementou.
        List<Doacao> doacoes = doacaoService.listarTodas();
        return ResponseEntity.ok(doacoes);
    }
}