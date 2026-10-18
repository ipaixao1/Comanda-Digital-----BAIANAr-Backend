package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Categoria;

public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        Integer ordem,
        String status
) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao(), c.getOrdem(), c.getStatus().name());
    }
}
