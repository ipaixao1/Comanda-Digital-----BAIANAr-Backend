package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.IngredienteRequest;
import com.baianar.comandadigital.dto.response.IngredienteResponse;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.service.IngredienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-027: CRUD de ingredientes. Apenas ADMIN/GERENTE. */
@RestController
@RequestMapping("/api/admin/ingredientes")
@RequiredArgsConstructor
@Tag(name = "Admin · Ingredientes")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class IngredienteController {

    private final IngredienteService ingredienteService;

    @GetMapping
    public ResponseEntity<PageResponse<IngredienteResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(ingredienteService.listar(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredienteResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ingredienteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<IngredienteResponse> criar(@Valid @RequestBody IngredienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredienteService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredienteResponse> atualizar(@PathVariable Long id, @Valid @RequestBody IngredienteRequest request) {
        return ResponseEntity.ok(ingredienteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        ingredienteService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
