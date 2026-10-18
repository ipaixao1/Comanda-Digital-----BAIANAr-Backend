package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;

/** RF-034: KPIs do dia. */
public record DashboardResumoResponse(
        BigDecimal faturamentoDoDia,
        long totalPedidosDoDia,
        BigDecimal ticketMedio,
        BigDecimal foodCostMedio
) {
}
