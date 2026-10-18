package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String perfil,
        String telefone,
        String status
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil().name(), u.getTelefone(), u.getStatus().name());
    }
}
