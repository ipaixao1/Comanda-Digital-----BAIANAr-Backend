package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro/edição de endereço de entrega do cliente. */
public record EnderecoRequest(
        @NotBlank(message = "Informe o nome do endereço") @Size(max = 60, message = "Nome do endereço: máximo 60 caracteres") String nome,
        @NotBlank(message = "Informe o CEP") @Pattern(regexp = "^\\d{5}-?\\d{3}$", message = "CEP inválido") String cep,
        @NotBlank(message = "Informe a rua") @Size(max = 150, message = "Rua: máximo 150 caracteres") String rua,
        @NotBlank(message = "Informe o número") @Size(max = 20, message = "Número: máximo 20 caracteres") String numero,
        @Size(max = 100, message = "Complemento: máximo 100 caracteres") String complemento,
        @NotBlank(message = "Informe o bairro") @Size(max = 100, message = "Bairro: máximo 100 caracteres") String bairro,
        @NotBlank(message = "Informe a cidade") @Size(max = 100, message = "Cidade: máximo 100 caracteres") String cidade,
        @NotBlank(message = "Informe a UF") @Pattern(regexp = "^[A-Za-z]{2}$", message = "UF deve ter 2 letras") String estado,
        Boolean padrao
) {
}
