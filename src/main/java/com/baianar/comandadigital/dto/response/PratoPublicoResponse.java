package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Prato;

import java.math.BigDecimal;

/** RF-001/RF-002: cardápio público — não expõe custo nem ficha técnica. */
public record PratoPublicoResponse(
        Long id,
        String nome,
        String descricao,
        String fotoUrl,
        BigDecimal precoVenda,
        Integer tempoPreparoMin,
        Long categoriaId,
        String categoriaNome
) {
    public static PratoPublicoResponse from(Prato p) {
        return new PratoPublicoResponse(
                p.getId(), p.getNome(), p.getDescricao(), p.getFotoUrl(),
                p.getPrecoVenda(), p.getTempoPreparoMin(),
                p.getCategoria().getId(), p.getCategoria().getNome()
        );
    }
}
