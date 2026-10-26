package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Pedido;
import com.baianar.comandadigital.util.EstimativaPedido;

import java.time.LocalDateTime;

/** RF-007: resposta enxuta para a timeline de acompanhamento do pedido pelo cliente (inclui a estimativa de tempo). */
public record PedidoStatusResponse(
        Long id,
        String status,
        LocalDateTime createdAt,
        Integer tempoPreparoMin,
        Integer tempoEntregaMin,
        Integer tempoTotalMin,
        LocalDateTime previsaoEntregaEm
) {
    public static PedidoStatusResponse from(Pedido p) {
        EstimativaPedido est = EstimativaPedido.calcular(p);
        return new PedidoStatusResponse(
                p.getId(), p.getStatus().name(), p.getCreatedAt(),
                est.preparoMin(), est.entregaMin(), est.totalMin(), est.previsaoEntregaEm(p.getCreatedAt())
        );
    }
}
