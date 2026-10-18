package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;

/** RF-031/RF-032: saldo em tempo real + indicador de alerta de mínimo. */
public record EstoqueSaldoResponse(
        Long ingredienteId,
        String nome,
        String sku,
        String unidadePadrao,
        BigDecimal saldoAtual,
        BigDecimal estoqueMinimo,
        boolean abaixoDoMinimo
) {
}
