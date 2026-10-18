package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.PedidoItem;

import java.math.BigDecimal;

public record PedidoItemResponse(
        Long id,
        Long pratoId,
        String pratoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        String observacoes
) {
    public static PedidoItemResponse from(PedidoItem item) {
        return new PedidoItemResponse(
                item.getId(), item.getPrato().getId(), item.getPrato().getNome(),
                item.getQuantidade(), item.getPrecoUnitario(), item.getObservacoes()
        );
    }
}
