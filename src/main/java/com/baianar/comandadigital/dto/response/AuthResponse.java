package com.baianar.comandadigital.dto.response;

public record AuthResponse(
        String token,
        Long id,
        String nome,
        String email,
        String perfil,
        String telefone,
        String endereco
) {
}
