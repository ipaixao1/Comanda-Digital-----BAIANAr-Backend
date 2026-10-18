package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;

public record PedidoCompraItemResponse(
        Long id,
        Long ingredienteId,
        String ingredienteNome,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
}
