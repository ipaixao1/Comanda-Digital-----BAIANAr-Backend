package com.baianar.comandadigital.dto.response;

import java.math.BigDecimal;
import java.util.List;

/** RF-023: cotação comparativa — todos os fornecedores que vendem um ingrediente, ordenados por preço. */
public record CotacaoResponse(
        Long ingredienteId,
        String ingredienteNome,
        List<Oferta> ofertas
) {
    public record Oferta(Long fornecedorId, String fornecedorNome, BigDecimal preco, String unidadeVenda) {
    }
}
