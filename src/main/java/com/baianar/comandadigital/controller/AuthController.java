package com.baianar.comandadigital.controller;

import com.baianar.comandadigital.dto.request.LoginRequest;
import com.baianar.comandadigital.dto.request.RegisterRequest;
import com.baianar.comandadigital.dto.response.AuthResponse;
import com.baianar.comandadigital.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RF-038 (login, todos os perfis) e RF-039 (cadastro público de cliente).
 * Infraestrutura compartilhada — base pronta para a Isabel evoluir a jornada do cliente.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login (todos os perfis)", description = "Retorna JWT com o perfil (role) no payload.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastro público de cliente", description = "Qualquer pessoa pode criar conta; perfil sempre CLIENTE.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
