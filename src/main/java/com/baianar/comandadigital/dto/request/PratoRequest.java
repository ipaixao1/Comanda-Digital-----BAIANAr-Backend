package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PratoRequest(
        @NotNull(message = "Categoria é obrigatória") Long categoriaId,
        @NotBlank(message = "Nome é obrigatório") String nome,
        String descricao,
        String fotoUrl,
        @NotNull @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero") BigDecimal precoVenda,
        @Positive(message = "Tempo de preparo deve ser positivo") Integer tempoPreparoMin,
        String status
) {
}
