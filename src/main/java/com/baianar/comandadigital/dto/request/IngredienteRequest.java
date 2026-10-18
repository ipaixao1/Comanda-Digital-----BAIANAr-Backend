package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record IngredienteRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "SKU é obrigatório") String sku,
        @NotNull(message = "Unidade padrão é obrigatória") String unidadePadrao,
        @NotNull @DecimalMin(value = "0", message = "Estoque mínimo não pode ser negativo") BigDecimal estoqueMinimo,
        @NotNull @DecimalMin(value = "0", message = "Custo unitário não pode ser negativo") BigDecimal custoUnitario
) {
}
