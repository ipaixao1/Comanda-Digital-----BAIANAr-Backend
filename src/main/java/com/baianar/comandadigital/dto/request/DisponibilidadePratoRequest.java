package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.NotNull;

/** Cozinha: pausa (false) ou reativa (true) um prato. */
public record DisponibilidadePratoRequest(
        @NotNull(message = "Informe se o prato está disponível") Boolean disponivel
) {
}
