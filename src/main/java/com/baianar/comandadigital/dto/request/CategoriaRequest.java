package com.baianar.comandadigital.dto.request;

import com.baianar.comandadigital.entity.enums.StatusCategoria;
import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        String descricao,
        Integer ordem,
        StatusCategoria status
) {
}