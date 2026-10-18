package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        String descricao,
        Integer ordem
) {
}
