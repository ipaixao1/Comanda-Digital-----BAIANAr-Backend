package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoItemRequest(
        @NotNull(message = "Prato é obrigatório") Long pratoId,
        @NotNull @Positive(message = "Quantidade deve ser maior que zero") Integer quantidade,
        String observacoes
) {
}
