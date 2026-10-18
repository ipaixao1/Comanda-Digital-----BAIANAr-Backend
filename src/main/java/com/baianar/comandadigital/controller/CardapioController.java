package com.baianar.comandadigital.controller;

import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.dto.response.PratoPublicoResponse;
import com.baianar.comandadigital.service.PratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RF-001/RF-002: cardápio público, sem login — infraestrutura compartilhada
 * (a Isabel pode evoluir esta tela conforme a jornada do cliente avança).
 */
@RestController
@RequestMapping("/api/cardapio")
@RequiredArgsConstructor
@Tag(name = "Cardápio Público")
@SecurityRequirements
public class CardapioController {

    private final PratoService pratoService;

    @GetMapping
    @Operation(summary = "Lista pratos ativos (público)", description = "Filtro opcional por categoria via ?categoriaId=")
    public ResponseEntity<PageResponse<PratoPublicoResponse>> listar(
            @RequestParam(required = false) Long categoriaId,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(pratoService.listarCardapioPublico(categoriaId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe do prato (público)")
    public ResponseEntity<PratoPublicoResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(pratoService.detalharPublico(id));
    }
}
