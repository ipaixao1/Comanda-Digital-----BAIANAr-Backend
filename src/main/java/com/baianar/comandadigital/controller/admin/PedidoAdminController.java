package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.CancelarPedidoRequest;
import com.baianar.comandadigital.dto.request.MudarStatusRequest;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.dto.response.PedidoResponse;
import com.baianar.comandadigital.entity.enums.StatusPedido;
import com.baianar.comandadigital.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * RF-015, RF-016, RF-017 (baixa automática), RF-018 (cancelamento c/ estorno), RF-019, RF-020.
 * Listagem/detalhe/mudança de status: ADMIN/GERENTE/COZINHEIRO. Cancelamento: só ADMIN/GERENTE (RN04).
 */
@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
@Tag(name = "Admin · Pedidos & Cozinha")
public class PedidoAdminController {

    private final PedidoService pedidoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHEIRO')")
    @Operation(summary = "RF-019: lista pedidos paginado, com filtro opcional por status e período")
    public ResponseEntity<PageResponse<PedidoResponse>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable) {
        StatusPedido statusEnum = status != null ? StatusPedido.valueOf(status.toUpperCase()) : null;
        return ResponseEntity.ok(PageResponse.from(pedidoService.listar(statusEnum, inicio, fim, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHEIRO')")
    @Operation(summary = "RF-020: detalhe completo do pedido")
    public ResponseEntity<PedidoResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.detalhar(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHEIRO')")
    @Operation(summary = "RF-016/RF-017: avança o status do pedido (CONFIRMADO dá baixa automática no estoque)")
    public ResponseEntity<PedidoResponse> mudarStatus(@PathVariable Long id, @Valid @RequestBody MudarStatusRequest request) {
        StatusPedido novoStatus = StatusPedido.valueOf(request.novoStatus().toUpperCase());
        return ResponseEntity.ok(pedidoService.mudarStatus(id, novoStatus));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "RF-018/RN04: cancela o pedido (estorna estoque se já havia baixa)")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id, @Valid @RequestBody CancelarPedidoRequest request) {
        return ResponseEntity.ok(pedidoService.cancelar(id, request.motivo()));
    }
}
