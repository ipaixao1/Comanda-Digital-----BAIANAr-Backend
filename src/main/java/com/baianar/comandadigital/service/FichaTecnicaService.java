package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.FichaTecnicaItemRequest;
import com.baianar.comandadigital.dto.request.FichaTecnicaRequest;
import com.baianar.comandadigital.dto.response.CustoResponse;
import com.baianar.comandadigital.dto.response.FichaTecnicaItemResponse;
import com.baianar.comandadigital.dto.response.FichaTecnicaResponse;
import com.baianar.comandadigital.entity.FichaTecnica;
import com.baianar.comandadigital.entity.FichaTecnicaItem;
import com.baianar.comandadigital.entity.Ingrediente;
import com.baianar.comandadigital.entity.Prato;
import com.baianar.comandadigital.entity.enums.UnidadeMedida;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.FichaTecnicaRepository;
import com.baianar.comandadigital.repository.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * RF-011 a RF-014, RN01, RN02, RN08.
 * Toda a lógica de cálculo de custo/food-cost mora aqui (nunca no Controller — RNF04).
 */
@Service
@RequiredArgsConstructor
public class FichaTecnicaService {

    private static final BigDecimal LIMITE_VERDE = new BigDecimal("30");
    private static final BigDecimal LIMITE_AMARELO = new BigDecimal("35");

    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final IngredienteRepository ingredienteRepository;

    @Transactional(readOnly = true)
    public FichaTecnica buscarEntidadePorPrato(Long pratoId) {
        return fichaTecnicaRepository.findByPratoId(pratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prato ainda não possui ficha técnica cadastrada."));
    }

    @Transactional(readOnly = true)
    public FichaTecnicaResponse buscarPorPrato(Long pratoId) {
        return toResponse(buscarEntidadePorPrato(pratoId));
    }

    @Transactional
    public FichaTecnicaResponse salvar(Prato prato, FichaTecnicaRequest request) {
        FichaTecnica ficha = fichaTecnicaRepository.findByPratoId(prato.getId())
                .orElseGet(() -> FichaTecnica.builder().prato(prato).build());

        ficha.setRendimento(request.rendimento());
        ficha.setModoPreparo(request.modoPreparo());

        ficha.getItens().clear();
        for (FichaTecnicaItemRequest itemReq : request.itens()) {
            Ingrediente ingrediente = ingredienteRepository.findById(itemReq.ingredienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrediente não encontrado: " + itemReq.ingredienteId()));

                    if ("INATIVO".equals(ingrediente.getStatus().name())) {
    throw new IllegalArgumentException(
            "Não é permitido adicionar ingrediente inativo à ficha técnica: " + ingrediente.getNome()
    );
}

            FichaTecnicaItem item = FichaTecnicaItem.builder()
                    .fichaTecnica(ficha)
                    .ingrediente(ingrediente)
                    .quantidade(itemReq.quantidade())
                    .unidade(UnidadeMedida.valueOf(itemReq.unidade().toUpperCase()))
                    .fatorCorrecao(itemReq.fatorCorrecao())
                    .build();
            ficha.getItens().add(item);
        }

        FichaTecnica salva = fichaTecnicaRepository.save(ficha);
        return toResponse(salva);
    }

        @Transactional(readOnly = true)
    public FichaTecnicaResponse simularCusto(Prato prato, FichaTecnicaRequest request) {
        FichaTecnica ficha = FichaTecnica.builder()
                .prato(prato)
                .rendimento(request.rendimento())
                .modoPreparo(request.modoPreparo())
                .build();

        for (FichaTecnicaItemRequest itemReq : request.itens()) {
            Ingrediente ingrediente = ingredienteRepository
                    .findById(itemReq.ingredienteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Ingrediente não encontrado: " + itemReq.ingredienteId()
                    ));

            FichaTecnicaItem item = FichaTecnicaItem.builder()
                    .fichaTecnica(ficha)
                    .ingrediente(ingrediente)
                    .quantidade(itemReq.quantidade())
                    .unidade(UnidadeMedida.valueOf(itemReq.unidade().toUpperCase()))
                    .fatorCorrecao(itemReq.fatorCorrecao())
                    .build();

            ficha.getItens().add(item);
        }

        return toResponse(ficha);
    }

    public CustoResponse calcularCusto(FichaTecnica ficha) {
        BigDecimal custoTotal = BigDecimal.ZERO;

        for (FichaTecnicaItem item : ficha.getItens()) {
            BigDecimal custoItem = item.getQuantidade()
                    .multiply(item.getFatorCorrecao())
                    .multiply(item.getIngrediente().getCustoUnitario());
            custoTotal = custoTotal.add(custoItem);
        }

        int rendimento = ficha.getRendimento() != null && ficha.getRendimento() > 0 ? ficha.getRendimento() : 1;
        BigDecimal custoPorPorcao = custoTotal.divide(BigDecimal.valueOf(rendimento), 4, RoundingMode.HALF_UP);

        BigDecimal precoVenda = ficha.getPrato().getPrecoVenda();
        BigDecimal foodCost = BigDecimal.ZERO;
        if (precoVenda != null && precoVenda.compareTo(BigDecimal.ZERO) > 0) {
            foodCost = custoPorPorcao.divide(precoVenda, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        String cor;
        if (foodCost.compareTo(LIMITE_VERDE) <= 0) {
            cor = "VERDE";
        } else if (foodCost.compareTo(LIMITE_AMARELO) <= 0) {
            cor = "AMARELO";
        } else {
            cor = "VERMELHO";
        }

        // RN02: food cost > 35% apenas gera aviso, não bloqueia.
        boolean alerta = foodCost.compareTo(LIMITE_AMARELO) > 0;

        return new CustoResponse(
                custoTotal.setScale(2, RoundingMode.HALF_UP),
                custoPorPorcao.setScale(2, RoundingMode.HALF_UP),
                foodCost,
                cor,
                alerta
        );
    }

    @Transactional(readOnly = true)
    public CustoResponse calcularCustoPorPrato(Long pratoId) {
        return calcularCusto(buscarEntidadePorPrato(pratoId));
    }

    private FichaTecnicaResponse toResponse(FichaTecnica ficha) {
        CustoResponse custo = calcularCusto(ficha);

        List<FichaTecnicaItemResponse> itens = ficha.getItens().stream()
                .map(item -> new FichaTecnicaItemResponse(
                        item.getId(),
                        item.getIngrediente().getId(),
                        item.getIngrediente().getNome(),
                        item.getQuantidade(),
                        item.getUnidade().name(),
                        item.getFatorCorrecao(),
                        item.getIngrediente().getCustoUnitario(),
                        item.getQuantidade().multiply(item.getFatorCorrecao()).multiply(item.getIngrediente().getCustoUnitario())
                                .setScale(2, RoundingMode.HALF_UP)
                ))
                .toList();

        return new FichaTecnicaResponse(ficha.getId(), ficha.getPrato().getId(), ficha.getRendimento(), ficha.getModoPreparo(), itens, custo);
    }
}
