package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RF-004/RF-039: cadastro público — sempre cria perfil CLIENTE. */
public record RegisterRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank @Email(message = "Email inválido") String email,
        @NotBlank @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres") String senha,
        @NotBlank(message = "Telefone é obrigatório") String telefone,
        @NotBlank(message = "Endereço de entrega é obrigatório") String endereco
) {
}
