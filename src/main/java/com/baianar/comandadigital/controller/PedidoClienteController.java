package com.baianar.comandadigital.controller;

import com.baianar.comandadigital.dto.request.CriarPedidoRequest;
import com.baianar.comandadigital.dto.response.PageResponse;
import com.baianar.comandadigital.dto.response.PedidoResponse;
import com.baianar.comandadigital.dto.response.PedidoStatusResponse;
import com.baianar.comandadigital.security.SecurityUtils;
import com.baianar.comandadigital.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ⚠️ JORNADA DO CLIENTE (Isabel) ⚠️
 * RF-006 (checkout), RF-007 (acompanhar status) e RF-008 (histórico/meus pedidos).
 *
 * Implementei um ponto de partida funcional, já ligado ao PedidoService (que contém a
 * validação de estoque RN03 e persiste o pedido como RECEBIDO), para o sistema ficar
 * testável de ponta a ponta. Esta classe é sua — sinta-se à vontade para ajustar o
 * contrato (DTOs em dto/request e dto/response), tratar observações do carrinho,
 * mensagens de erro específicas da UX mobile etc.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Cliente · Pedidos")
@PreAuthorize("hasRole('CLIENTE')")
public class PedidoClienteController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "RF-006: cria o pedido a partir do carrinho (checkout)")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody CriarPedidoRequest request) {
        Long clienteId = SecurityUtils.getCurrentUserId();
        PedidoResponse response = pedidoService.criar(clienteId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meus")
    @Operation(summary = "RF-008: histórico de pedidos do cliente logado")
    public ResponseEntity<PageResponse<PedidoResponse>> meusPedidos(Pageable pageable) {
        Long clienteId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(PageResponse.from(pedidoService.listarMeusPedidos(clienteId, pageable)));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "RF-007: status atual do pedido, para a timeline de acompanhamento")
    public ResponseEntity<PedidoStatusResponse> status(@PathVariable Long id) {
        Long clienteId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(pedidoService.buscarStatus(id, clienteId));
    }
}
