package com.baianar.comandadigital.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RF-041: ADMIN cria/edita usuários internos (GERENTE ou COZINHEIRO). */
public record UsuarioInternoRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank @Email(message = "Email inválido") String email,
        String senha,
        @NotBlank @Pattern(regexp = "GERENTE|COZINHEIRO", message = "Perfil deve ser GERENTE ou COZINHEIRO") String perfil,
        String telefone
) {
}
