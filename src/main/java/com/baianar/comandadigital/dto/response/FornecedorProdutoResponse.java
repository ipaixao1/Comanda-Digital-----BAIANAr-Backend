package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.FornecedorProduto;

import java.math.BigDecimal;

public record FornecedorProdutoResponse(
        Long id,
        Long fornecedorId,
        String fornecedorNome,
        Long ingredienteId,
        String ingredienteNome,
        BigDecimal preco,
        String unidadeVenda
) {
    public static FornecedorProdutoResponse from(FornecedorProduto fp) {
        return new FornecedorProdutoResponse(
                fp.getId(),
                fp.getFornecedor().getId(), fp.getFornecedor().getRazaoSocial(),
                fp.getIngrediente().getId(), fp.getIngrediente().getNome(),
                fp.getPreco(), fp.getUnidadeVenda().name()
        );
    }
}
