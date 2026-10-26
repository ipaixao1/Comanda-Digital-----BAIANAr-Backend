package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.response.FaixaEntregaResponse;
import com.baianar.comandadigital.entity.enums.StatusPrato;
import com.baianar.comandadigital.repository.PratoRepository;
import com.baianar.comandadigital.util.EstimativaPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Faixa de entrega exibida no cardápio (estilo iFood: "Entrega 20–30 min"). */
@Service
@RequiredArgsConstructor
public class EstimativaLojaService {

    private final PratoRepository pratoRepository;

    @Transactional(readOnly = true)
    public FaixaEntregaResponse faixaEntrega() {
        int[] faixa = EstimativaPedido.faixaLoja(pratoRepository.temposDePreparoPorStatus(StatusPrato.ATIVO));
        return new FaixaEntregaResponse(faixa[0], faixa[1]);
    }
}
