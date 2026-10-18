package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Ingrediente;

import java.math.BigDecimal;

public record IngredienteResponse(
        Long id,
        String nome,
        String sku,
        String unidadePadrao,
        BigDecimal estoqueMinimo,
        BigDecimal custoUnitario,
        String status,
        BigDecimal saldoAtual
) {
    public static IngredienteResponse from(Ingrediente i, BigDecimal saldoAtual) {
        return new IngredienteResponse(
                i.getId(), i.getNome(), i.getSku(), i.getUnidadePadrao().name(),
                i.getEstoqueMinimo(), i.getCustoUnitario(), i.getStatus().name(), saldoAtual
        );
    }
}
