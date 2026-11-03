package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(
        @NotBlank(message = "Razão social é obrigatória") String razaoSocial,
        @NotBlank(message = "CNPJ é obrigatório") String cnpj,
        String telefone,
        @Email(message = "Email inválido") String email,
        @Size(max = 200, message = "Categorias deve ter no máximo 200 caracteres") String categorias
) {
}
