package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Prato;

import java.math.BigDecimal;

public record PratoResponse(
        Long id,
        Long categoriaId,
        String categoriaNome,
        String nome,
        String descricao,
        String fotoUrl,
        BigDecimal precoVenda,
        Integer tempoPreparoMin,
        String status,
        boolean temFichaTecnica
) {
    public static PratoResponse from(Prato p) {
        return new PratoResponse(
                p.getId(),
                p.getCategoria().getId(),
                p.getCategoria().getNome(),
                p.getNome(),
                p.getDescricao(),
                p.getFotoUrl(),
                p.getPrecoVenda(),
                p.getTempoPreparoMin(),
                p.getStatus().name(),
                p.getFichaTecnica() != null
        );
    }
}
