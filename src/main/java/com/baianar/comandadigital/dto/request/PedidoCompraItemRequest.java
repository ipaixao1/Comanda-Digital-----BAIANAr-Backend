package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PedidoCompraItemRequest(
        @NotNull(message = "Ingrediente é obrigatório") Long ingredienteId,
        @NotNull @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade
) {
}
