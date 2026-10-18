package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RF-037: faturamento diário, para gráfico de linha. */
public record VendaPeriodoResponse(
        LocalDate dia,
        BigDecimal total
) {
}
