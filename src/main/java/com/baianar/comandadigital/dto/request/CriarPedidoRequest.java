package com.baianar.comandadigital.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * RF-006: checkout — resumo do carrinho (RF-003, montado no front) + endereço de entrega.
 * Pagamento é simulado (RNF/SRS §1.2): não há integração real, por isso não há campo de pagamento aqui.
 */
public record CriarPedidoRequest(
        @NotEmpty(message = "O pedido precisa de ao menos 1 item")
        @Valid List<PedidoItemRequest> itens,
        @NotBlank(message = "Endereço de entrega é obrigatório") String enderecoEntrega,
        String observacoes
) {
}
