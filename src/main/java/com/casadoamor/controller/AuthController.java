package com.casadoamor.controller;

import com.casadoamor.dto.LoginRequest;
import com.casadoamor.dto.LoginResponse;
import com.casadoamor.service.AutenticacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Importante para o front (React/Angular) conseguir acessar
public class AuthController {

    private final AutenticacaoService authService;

    public AuthController() {
        this.authService = new AutenticacaoService();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Tenta autenticar
            LoginResponse response = authService.autenticar(loginRequest);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Se falhar (senha errada ou não é admin), devolve erro 401
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
