package com.baianar.comandadigital.dto.response;

/** Faixa de tempo de entrega da loja, exibida no cardápio (ex.: 20–30 min). */
public record FaixaEntregaResponse(Integer minimoMin, Integer maximoMin) {
}
