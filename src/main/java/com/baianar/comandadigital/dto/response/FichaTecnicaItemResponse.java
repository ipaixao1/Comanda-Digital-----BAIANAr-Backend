package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;

public record FichaTecnicaItemResponse(
        Long id,
        Long ingredienteId,
        String ingredienteNome,
        BigDecimal quantidade,
        String unidade,
        BigDecimal fatorCorrecao,
        BigDecimal custoUnitario,
        BigDecimal custoItem
) {
}
