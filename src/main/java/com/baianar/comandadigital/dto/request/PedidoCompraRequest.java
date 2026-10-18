package com.baianar.comandadigital.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoCompraRequest(
        @NotNull(message = "Fornecedor é obrigatório") Long fornecedorId,
        @NotEmpty(message = "O pedido de compra precisa de ao menos 1 item")
        @Valid List<PedidoCompraItemRequest> itens
) {
}
