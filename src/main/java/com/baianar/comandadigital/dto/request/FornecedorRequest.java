package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FornecedorRequest(
        @NotBlank(message = "Razão social é obrigatória") String razaoSocial,
        @NotBlank(message = "CNPJ é obrigatório") String cnpj,
        String telefone,
        @Email(message = "Email inválido") String email
) {
}
