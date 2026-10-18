package com.baianar.comandadigital.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record FichaTecnicaRequest(
        @NotNull @Positive(message = "Rendimento deve ser positivo") Integer rendimento,
        String modoPreparo,
        // RN01: prato só fica ATIVO se tiver ficha técnica com pelo menos 1 ingrediente
        @NotEmpty(message = "A ficha técnica precisa de ao menos 1 ingrediente")
        @Valid List<FichaTecnicaItemRequest> itens
) {
}
