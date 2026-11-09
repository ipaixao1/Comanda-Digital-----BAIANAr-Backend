package com.baianar.comandadigital.controller;

import com.baianar.comandadigital.dto.request.EnderecoRequest;
import com.baianar.comandadigital.dto.response.EnderecoResponse;
import com.baianar.comandadigital.security.SecurityUtils;
import com.baianar.comandadigital.service.EnderecoClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Jornada do cliente: tela "Meus Endereços" e escolha do endereço no checkout. */
@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
@Tag(name = "Cliente · Endereços")
@PreAuthorize("hasRole('CLIENTE')")
public class EnderecoClienteController {

    private final EnderecoClienteService service;

    @GetMapping
    @Operation(summary = "Lista os endereços do cliente logado (o padrão vem primeiro)")
    public List<EnderecoResponse> listar() {
        return service.listar(SecurityUtils.getCurrentUserId());
    }

    @PostMapping
    @Operation(summary = "Adiciona um endereço (o primeiro vira o padrão automaticamente)")
    public ResponseEntity<EnderecoResponse> criar(@Valid @RequestBody EnderecoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(SecurityUtils.getCurrentUserId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita um endereço do cliente logado")
    public EnderecoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EnderecoRequest request) {
        return service.atualizar(SecurityUtils.getCurrentUserId(), id, request);
    }

    @PatchMapping("/{id}/padrao")
    @Operation(summary = "Define o endereço como padrão")
    public EnderecoResponse definirPadrao(@PathVariable Long id) {
        return service.definirPadrao(SecurityUtils.getCurrentUserId(), id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um endereço (se era o padrão, o mais recente restante assume)")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
