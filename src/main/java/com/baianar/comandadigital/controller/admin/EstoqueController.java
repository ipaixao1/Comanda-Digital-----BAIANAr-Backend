package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.MovimentacaoManualRequest;
import com.baianar.comandadigital.dto.response.EstoqueSaldoResponse;
import com.baianar.comandadigital.dto.response.MovimentacaoResponse;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.service.EstoqueService;
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

/** RF-028 a RF-033: controle de estoque. Apenas ADMIN/GERENTE. */
@RestController
@RequestMapping("/api/admin/estoque")
@RequiredArgsConstructor
@Tag(name = "Admin · Estoque")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping("/saldo")
    @Operation(summary = "RF-031: saldo atual de todos os ingredientes")
    public ResponseEntity<List<EstoqueSaldoResponse>> saldo() {
        return ResponseEntity.ok(estoqueService.saldoGeral());
    }

    @GetMapping("/alertas")
    @Operation(summary = "RF-032: ingredientes abaixo do estoque mínimo")
    public ResponseEntity<List<EstoqueSaldoResponse>> alertas() {
        return ResponseEntity.ok(estoqueService.alertasMinimo());
    }

    @GetMapping("/movimentacao")
    @Operation(summary = "RF-033: histórico paginado de movimentações")
    public ResponseEntity<PageResponse<MovimentacaoResponse>> movimentacoes(
            @RequestParam(required = false) Long ingredienteId, Pageable pageable) {
        var page = ingredienteId != null
                ? estoqueService.listarMovimentacoesPorIngrediente(ingredienteId, pageable)
                : estoqueService.listarMovimentacoes(pageable);
        return ResponseEntity.ok(PageResponse.from(page));
    }

    @GetMapping("/{ingredienteId}/historico-precos")
    @Operation(summary = "RF-026: histórico de preços de compra do ingrediente (para gráfico de evolução)")
    public ResponseEntity<List<MovimentacaoResponse>> historicoPrecos(@PathVariable Long ingredienteId) {
        return ResponseEntity.ok(estoqueService.historicoPrecos(ingredienteId));
    }

    @PostMapping("/movimentacao")
    @Operation(summary = "RF-030: registra saída manual (perda) ou entrada/ajuste manual")
    public ResponseEntity<MovimentacaoResponse> registrarMovimentacao(@Valid @RequestBody MovimentacaoManualRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueService.registrarMovimentacaoManual(request));
    }
}
