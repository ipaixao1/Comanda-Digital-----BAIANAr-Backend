package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-028 (entrada manual/ajuste) e RF-030 (saída manual — motivo obrigatório). */
public record MovimentacaoManualRequest(
        @NotNull(message = "Ingrediente é obrigatório") Long ingredienteId,
        @NotNull(message = "Tipo é obrigatório (ENTRADA ou SAIDA)") String tipo,
        @NotNull @DecimalMin(value = "0.001", message = "Quantidade deve ser maior que zero") BigDecimal quantidade,
        @NotNull(message = "Motivo é obrigatório") String motivo,
        String lote,
        LocalDate validade,
        BigDecimal custoUnitario
) {
}
