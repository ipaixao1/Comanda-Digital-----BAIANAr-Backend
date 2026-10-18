package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.EstoqueMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovimentacaoResponse(
        Long id,
        Long ingredienteId,
        String ingredienteNome,
        String tipo,
        BigDecimal quantidade,
        String motivo,
        String lote,
        LocalDate validade,
        BigDecimal custoUnitario,
        Long pedidoCompraId,
        Long pedidoId,
        String usuarioNome,
        LocalDateTime createdAt
) {
    public static MovimentacaoResponse from(EstoqueMovimentacao m) {
        return new MovimentacaoResponse(
                m.getId(),
                m.getIngrediente().getId(),
                m.getIngrediente().getNome(),
                m.getTipo().name(),
                m.getQuantidade(),
                m.getMotivo().name(),
                m.getLote(),
                m.getValidade(),
                m.getCustoUnitario(),
                m.getPedidoCompra() != null ? m.getPedidoCompra().getId() : null,
                m.getPedido() != null ? m.getPedido().getId() : null,
                m.getUsuario().getNome(),
                m.getCreatedAt()
        );
    }
}
