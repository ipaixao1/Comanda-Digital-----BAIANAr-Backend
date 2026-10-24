package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FichaTecnicaItemRequest(
        @NotNull(message = "Ingrediente é obrigatório") Long ingredienteId,
        @NotNull @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotBlank(message = "Unidade é obrigatória") String unidade,
        // RN08: fator de correção >= 1.0
        @NotNull @DecimalMin(value = "1.0", message = "Fator de correção não pode ser menor que 1.0") BigDecimal fatorCorrecao
) {
}
