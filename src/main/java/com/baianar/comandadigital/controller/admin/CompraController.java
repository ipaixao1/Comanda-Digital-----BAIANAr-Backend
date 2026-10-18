package com.baianar.comandadigital.controller.admin;

import com.baianar.comandadigital.dto.request.PedidoCompraRequest;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.dto.response.PedidoCompraResponse;
import com.baianar.comandadigital.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** RF-024 a RF-026: pedidos de compra ao fornecedor. Apenas ADMIN/GERENTE. */
@RestController
@RequestMapping("/api/admin/compras")
@RequiredArgsConstructor
@Tag(name = "Admin · Compras")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<PageResponse<PedidoCompraResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(compraService.listar(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoCompraResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "RF-024: cria pedido de compra (status inicial RASCUNHO)")
    public ResponseEntity<PedidoCompraResponse> criar(@Valid @RequestBody PedidoCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.criar(request));
    }

    @PatchMapping("/{id}/enviar")
    @Operation(summary = "RF-024: RASCUNHO > ENVIADO")
    public ResponseEntity<PedidoCompraResponse> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.enviar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        compraService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/receber")
    @Operation(summary = "RF-025/RN05: registra recebimento — cria entrada no estoque e atualiza custo do ingrediente")
    public ResponseEntity<PedidoCompraResponse> receber(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.receber(id));
    }
}
