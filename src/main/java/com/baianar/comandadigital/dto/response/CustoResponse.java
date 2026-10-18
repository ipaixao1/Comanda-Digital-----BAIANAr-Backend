package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;

/**
 * RF-012/RF-013: custo = SUM(qtd × fator_correcao × custo_unitario) / rendimento;
 * food_cost (%) = (custo / preco_venda) × 100. Cor: verde ≤30%, amarelo 31-35%, vermelho >35%.
 * RN02: alerta (não bloqueio) quando food cost > 35%.
 */
public record CustoResponse(
        BigDecimal custoTotal,
        BigDecimal custoPorPorcao,
        BigDecimal foodCostPercent,
        String cor,
        boolean alertaFoodCostAlto
) {
}
