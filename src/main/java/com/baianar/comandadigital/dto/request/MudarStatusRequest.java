package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MudarStatusRequest(
        @NotBlank(message = "Novo status é obrigatório") String novoStatus
) {
}
