package com.baianar.comandadigital.controller.admin;

import java.math.BigDecimal;
import com.baianar.comandadigital.dto.request.FichaTecnicaRequest;
import com.baianar.comandadigital.dto.request.PratoRequest;
import com.baianar.comandadigital.dto.response.CustoResponse;
import com.baianar.comandadigital.dto.response.FichaTecnicaResponse;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.dto.response.PratoResponse;
import com.baianar.comandadigital.entity.Prato;
import com.baianar.comandadigital.service.FichaTecnicaService;
import com.baianar.comandadigital.service.PratoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-010 a RF-014: CRUD de pratos + ficha técnica + custo. Apenas ADMIN/GERENTE. */
@RestController
@RequestMapping("/api/admin/pratos")
@RequiredArgsConstructor
@Tag(name = "Admin · Pratos & Ficha Técnica")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class PratoController {

    private final PratoService pratoService;
    private final FichaTecnicaService fichaTecnicaService;

    @GetMapping
    public ResponseEntity<PageResponse<PratoResponse>> listar(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(pratoService.listar(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PratoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pratoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PratoResponse> criar(@Valid @RequestBody PratoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pratoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PratoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody PratoRequest request) {
        return ResponseEntity.ok(pratoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa o prato (exclusão lógica — RN06)")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        pratoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/custo")
    @Operation(summary = "RF-013: custo calculado + food cost (%) com cor")
    public ResponseEntity<CustoResponse> custo(@PathVariable Long id) {
        return ResponseEntity.ok(fichaTecnicaService.calcularCustoPorPrato(id));
    }

@PostMapping("/{id}/custo/simular")
@Operation(summary = "RF-012: simula o custo da ficha técnica sem salvar")
public ResponseEntity<FichaTecnicaResponse> simularCusto(
        @PathVariable Long id,
        @Valid @RequestBody FichaTecnicaRequest request) {

    Prato prato = pratoService.buscarEntidade(id);

    return ResponseEntity.ok(
            fichaTecnicaService.simularCusto(prato, request)
    );
}

@PostMapping("/custo/simular")
@Operation(summary = "RF-012: simula o custo de um prato novo, sem salvar")
public ResponseEntity<FichaTecnicaResponse> simularCustoNovoPrato(
        @RequestParam BigDecimal precoVenda,
        @Valid @RequestBody FichaTecnicaRequest request) {

    if (precoVenda == null || precoVenda.compareTo(BigDecimal.ZERO) < 0) {
    return ResponseEntity.badRequest().build();
}

    Prato pratoTemporario = Prato.builder()
            .precoVenda(precoVenda)
            .build();

    return ResponseEntity.ok(
            fichaTecnicaService.simularCusto(pratoTemporario, request)
    );
}

    @GetMapping("/{id}/ficha")
    @Operation(summary = "RF-011: obtém a ficha técnica do prato")
    public ResponseEntity<FichaTecnicaResponse> obterFicha(@PathVariable Long id) {
        return ResponseEntity.ok(fichaTecnicaService.buscarPorPrato(id));
    }

    @PutMapping("/{id}/ficha")
    @Operation(summary = "RF-011: cria/atualiza a ficha técnica (lista de ingredientes + rendimento + modo de preparo)")
    public ResponseEntity<FichaTecnicaResponse> salvarFicha(@PathVariable Long id, @Valid @RequestBody FichaTecnicaRequest request) {
        Prato prato = pratoService.buscarEntidade(id);
        return ResponseEntity.ok(fichaTecnicaService.salvar(prato, request));
    }
}
