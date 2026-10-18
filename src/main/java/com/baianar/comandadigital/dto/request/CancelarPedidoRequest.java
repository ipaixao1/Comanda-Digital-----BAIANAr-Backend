package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.NotBlank;

/** RF-018: cancelamento exige motivo. */
public record CancelarPedidoRequest(
        @NotBlank(message = "Motivo do cancelamento é obrigatório") String motivo
) {
}
