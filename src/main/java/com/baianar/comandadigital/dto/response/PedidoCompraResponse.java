package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoCompraResponse(
        Long id,
        Long fornecedorId,
        String fornecedorNome,
        String status,
        BigDecimal valorTotal,
        List<PedidoCompraItemResponse> itens,
        LocalDateTime createdAt
) {
}
