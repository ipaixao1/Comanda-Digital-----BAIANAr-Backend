package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FichaTecnicaResponse(
        Long id,
        Long pratoId,
        Integer rendimento,
        String modoPreparo,
        List<FichaTecnicaItemResponse> itens,
        CustoResponse custo
) {
}
