package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.CategoriaRequest;
import com.baianar.comandadigital.dto.response.CategoriaResponse;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-009 — CRUD de categorias. Apenas ADMIN/GERENTE (RF-043). */
@RestController
@RequestMapping("/api/admin/categorias")
@RequiredArgsConstructor
@Tag(name = "Admin · Categorias")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Lista categorias (paginado)")
    public ResponseEntity<PageResponse<CategoriaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(categoriaService.listar(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa a categoria (exclusão lógica — RN06)")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        categoriaService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
