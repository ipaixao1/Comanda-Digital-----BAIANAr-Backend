package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.IngredienteRequest;
import com.baianar.comandadigital.dto.response.IngredienteResponse;
import com.baianar.comandadigital.entity.Ingrediente;
import com.baianar.comandadigital.entity.enums.StatusIngrediente;
import com.baianar.comandadigital.entity.enums.UnidadeMedida;
import com.baianar.comandadigital.exception.ConflictException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-027: CRUD de ingredientes. */
@Service
@RequiredArgsConstructor
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final EstoqueService estoqueService;

    public Page<IngredienteResponse> listar(Pageable pageable) {
        return ingredienteRepository.findAll(pageable)
                .map(i -> IngredienteResponse.from(i, estoqueService.saldoAtual(i.getId())));
    }

    public IngredienteResponse buscarPorId(Long id) {
        Ingrediente ingrediente = buscarEntidade(id);
        return IngredienteResponse.from(ingrediente, estoqueService.saldoAtual(id));
    }

    public Ingrediente buscarEntidade(Long id) {
        return ingredienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente não encontrado: " + id));
    }

    @Transactional
    public IngredienteResponse criar(IngredienteRequest request) {
        if (ingredienteRepository.existsBySku(request.sku())) {
            throw new ConflictException("Já existe um ingrediente com o SKU '" + request.sku() + "'.");
        }

        Ingrediente ingrediente = Ingrediente.builder()
                .nome(request.nome())
                .sku(request.sku())
                .unidadePadrao(UnidadeMedida.valueOf(request.unidadePadrao().toUpperCase()))
                .estoqueMinimo(request.estoqueMinimo())
                .custoUnitario(request.custoUnitario())
                .status(StatusIngrediente.ATIVO)
                .build();

        ingrediente = ingredienteRepository.save(ingrediente);
        return IngredienteResponse.from(ingrediente, java.math.BigDecimal.ZERO);
    }

    @Transactional
    public IngredienteResponse atualizar(Long id, IngredienteRequest request) {
        Ingrediente ingrediente = buscarEntidade(id);

        if (!ingrediente.getSku().equals(request.sku()) && ingredienteRepository.existsBySku(request.sku())) {
            throw new ConflictException("Já existe um ingrediente com o SKU '" + request.sku() + "'.");
        }

        ingrediente.setNome(request.nome());
        ingrediente.setSku(request.sku());
        ingrediente.setUnidadePadrao(UnidadeMedida.valueOf(request.unidadePadrao().toUpperCase()));
        ingrediente.setEstoqueMinimo(request.estoqueMinimo());
        ingrediente.setCustoUnitario(request.custoUnitario());

        ingrediente = ingredienteRepository.save(ingrediente);
        return IngredienteResponse.from(ingrediente, estoqueService.saldoAtual(id));
    }

    /** RN06: exclusão lógica. */
    @Transactional
    public void desativar(Long id) {
        Ingrediente ingrediente = buscarEntidade(id);
        ingrediente.setStatus(StatusIngrediente.INATIVO);
        ingredienteRepository.save(ingrediente);
    }

    /** Usado pelo CompraService (RN05) ao registrar recebimento. */
    @Transactional
    public void atualizarCustoUnitario(Long id, java.math.BigDecimal novoCusto) {
        Ingrediente ingrediente = buscarEntidade(id);
        ingrediente.setCustoUnitario(novoCusto);
        ingredienteRepository.save(ingrediente);
    }
}
