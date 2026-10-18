package com.baianar.comandadigital.dto.response;

/** RF-035: ranking dos mais vendidos no período selecionado. */
public record TopPratoResponse(
        Long pratoId,
        String nome,
        long totalVendido
) {
}
