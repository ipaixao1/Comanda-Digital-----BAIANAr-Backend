package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.Fornecedor;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        String telefone,
        String email,
        String status
) {
    public static FornecedorResponse from(Fornecedor f) {
        return new FornecedorResponse(f.getId(), f.getRazaoSocial(), f.getCnpj(), f.getTelefone(), f.getEmail(), f.getStatus().name());
    }
}
