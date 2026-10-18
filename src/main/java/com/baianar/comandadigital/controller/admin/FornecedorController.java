package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.FornecedorProdutoRequest;
import com.baianar.comandadigital.dto.request.FornecedorRequest;
import com.baianar.comandadigital.dto.response.*;
import com.baianar.comandadigital.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF-021 a RF-023: fornecedores, catálogo e cotação comparativa. Apenas ADMIN/GERENTE. */
@RestController
@RequiredArgsConstructor
@Tag(name = "Admin · Fornecedores")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping("/api/admin/fornecedores")
    public ResponseEntity<PageResponse<FornecedorResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(fornecedorService.listar(pageable)));
    }

    @GetMapping("/api/admin/fornecedores/{id}")
    public ResponseEntity<FornecedorResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PostMapping("/api/admin/fornecedores")
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.criar(request));
    }

    @PutMapping("/api/admin/fornecedores/{id}")
    public ResponseEntity<FornecedorResponse> atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/api/admin/fornecedores/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        fornecedorService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/fornecedores/{id}/catalogo")
    @Operation(summary = "RF-022: catálogo de ingredientes vendidos pelo fornecedor")
    public ResponseEntity<List<FornecedorProdutoResponse>> catalogo(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.listarCatalogo(id));
    }

    @PostMapping("/api/admin/fornecedores/{id}/catalogo")
    @Operation(summary = "RF-022: adiciona/atualiza um ingrediente no catálogo do fornecedor")
    public ResponseEntity<FornecedorProdutoResponse> adicionarAoCatalogo(
            @PathVariable Long id, @Valid @RequestBody FornecedorProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.adicionarAoCatalogo(id, request));
    }

    @GetMapping("/api/admin/cotacao/{ingredienteId}")
    @Operation(summary = "RF-023: cotação comparativa entre fornecedores de um ingrediente")
    public ResponseEntity<CotacaoResponse> cotacao(@PathVariable Long ingredienteId) {
        return ResponseEntity.ok(fornecedorService.cotacao(ingredienteId));
    }
}
