package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Pedido;
import com.baianar.comandadigital.util.EstimativaPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long clienteId,
        String clienteNome,
        String status,
        BigDecimal valorTotal,
        String enderecoEntrega,
        String observacoes,
        String motivoCancelamento,
        List<PedidoItemResponse> itens,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        /** Estimativa em minutos (ver EstimativaPedido). */
        Integer tempoPreparoMin,
        Integer tempoEntregaMin,
        Integer tempoTotalMin,
        /** createdAt + tempoTotalMin; o front mostra "chega às HH:mm". */
        LocalDateTime previsaoEntregaEm
) {
    public static PedidoResponse from(Pedido p) {
        List<PedidoItemResponse> itens = p.getItens().stream().map(PedidoItemResponse::from).toList();
        EstimativaPedido est = EstimativaPedido.calcular(p);
        return new PedidoResponse(
                p.getId(), p.getCliente().getId(), p.getCliente().getNome(), p.getStatus().name(),
                p.getValorTotal(), p.getEnderecoEntrega(), p.getObservacoes(), p.getMotivoCancelamento(),
                itens, p.getCreatedAt(), p.getUpdatedAt(),
                est.preparoMin(), est.entregaMin(), est.totalMin(), est.previsaoEntregaEm(p.getCreatedAt())
        );
    }
}
