package com.baianar.comandadigital.dto.response;

/** RF-007: resposta enxuta para a timeline de acompanhamento do pedido pelo cliente. */
public record PedidoStatusResponse(
        Long id,
        String status
) {
}
