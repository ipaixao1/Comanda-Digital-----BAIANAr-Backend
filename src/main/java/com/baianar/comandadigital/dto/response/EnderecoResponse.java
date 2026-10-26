package com.baianar.comandadigital.dto.response;

import com.baianar.comandadigital.entity.EnderecoCliente;

public record EnderecoResponse(
        Long id,
        String nome,
        String cep,
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        boolean padrao
) {
    public static EnderecoResponse from(EnderecoCliente e) {
        return new EnderecoResponse(e.getId(), e.getNome(), e.getCep(), e.getRua(), e.getNumero(),
                e.getComplemento(), e.getBairro(), e.getCidade(), e.getEstado(), e.isPadrao());
    }
}
