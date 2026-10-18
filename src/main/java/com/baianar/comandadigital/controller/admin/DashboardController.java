package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.response.*;
import com.baianar.comandadigital.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** RF-034 a RF-037: dashboard. Apenas ADMIN/GERENTE. */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin · Dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @Operation(summary = "RF-034: KPIs do dia (faturamento, pedidos, ticket médio, food cost médio)")
    public ResponseEntity<DashboardResumoResponse> resumo() {
        return ResponseEntity.ok(dashboardService.resumoDoDia());
    }

    @GetMapping("/top-pratos")
    @Operation(summary = "RF-035: top 5 pratos mais vendidos no período")
    public ResponseEntity<List<TopPratoResponse>> topPratos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(dashboardService.topPratos(inicio, fim, limite));
    }

    @GetMapping("/alertas-estoque")
    @Operation(summary = "RF-036: ingredientes críticos, para exibir direto no dashboard")
    public ResponseEntity<List<EstoqueSaldoResponse>> alertasEstoque() {
        return ResponseEntity.ok(dashboardService.alertasEstoque());
    }

    @GetMapping("/vendas-periodo")
    @Operation(summary = "RF-037: faturamento diário no período, para gráfico de linha")
    public ResponseEntity<List<VendaPeriodoResponse>> vendasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return ResponseEntity.ok(dashboardService.vendasPorPeriodo(inicio, fim));
    }
}
