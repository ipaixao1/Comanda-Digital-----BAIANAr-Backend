package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** RF-022: vincula um ingrediente ao catálogo de um fornecedor. */
public record FornecedorProdutoRequest(
        @NotNull(message = "Ingrediente é obrigatório") Long ingredienteId,
        @NotNull @DecimalMin(value = "0.0001", message = "Preço deve ser maior que zero") BigDecimal preco,
        @NotNull(message = "Unidade de venda é obrigatória") String unidadeVenda
) {
}
