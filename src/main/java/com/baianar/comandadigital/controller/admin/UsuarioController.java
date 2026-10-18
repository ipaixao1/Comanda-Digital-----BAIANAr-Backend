package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.UsuarioInternoRequest;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.dto.response.UsuarioResponse;
import com.baianar.comandadigital.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-041: CRUD de usuários internos. Apenas ADMIN. */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Admin · Usuários")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PageResponse<UsuarioResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(usuarioService.listarInternos(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria usuário interno (GERENTE ou COZINHEIRO) — senha com BCrypt")
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioInternoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioInternoRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa o usuário (exclusão lógica — RN06)")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
