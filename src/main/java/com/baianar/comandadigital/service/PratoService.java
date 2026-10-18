package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.PratoRequest;
import com.baianar.comandadigital.dto.response.PratoPublicoResponse;
import com.baianar.comandadigital.dto.response.PratoResponse;
import com.baianar.comandadigital.entity.Categoria;
import com.baianar.comandadigital.entity.FichaTecnica;
import com.baianar.comandadigital.entity.Prato;
import com.baianar.comandadigital.entity.enums.StatusPrato;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.FichaTecnicaRepository;
import com.baianar.comandadigital.repository.PratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-010: CRUD de pratos. RN01/RN09 aplicadas aqui (nunca no Controller). */
@Service
@RequiredArgsConstructor
public class PratoService {

    private final PratoRepository pratoRepository;
    private final CategoriaService categoriaService;
    private final FichaTecnicaRepository fichaTecnicaRepository;

    @Transactional(readOnly = true)
    public Page<PratoResponse> listar(Pageable pageable) {
        return pratoRepository.findAllByOrderByIdAsc(pageable).map(PratoResponse::from);
    }

    @Transactional(readOnly = true)
    public PratoResponse buscarPorId(Long id) {
        return PratoResponse.from(buscarEntidade(id));
    }

    public Prato buscarEntidade(Long id) {
        return pratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prato não encontrado: " + id));
    }

    /** RF-001/RN09: cardápio público só lista pratos ATIVO, com filtro opcional por categoria. */
    @Transactional(readOnly = true)
    public Page<PratoPublicoResponse> listarCardapioPublico(Long categoriaId, Pageable pageable) {
        Page<Prato> pratos = categoriaId != null
                ? pratoRepository.findByStatusAndCategoriaId(StatusPrato.ATIVO, categoriaId, pageable)
                : pratoRepository.findByStatus(StatusPrato.ATIVO, pageable);
        return pratos.map(PratoPublicoResponse::from);
    }

    /** RF-002: detalhe público — só é visível se o prato estiver ATIVO. */
    @Transactional(readOnly = true)
    public PratoPublicoResponse detalharPublico(Long id) {
        Prato prato = buscarEntidade(id);
        if (prato.getStatus() != StatusPrato.ATIVO) {
            throw new ResourceNotFoundException("Prato não encontrado ou indisponível.");
        }
        return PratoPublicoResponse.from(prato);
    }

    @Transactional
    public PratoResponse criar(PratoRequest request) {
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId());

        Prato prato = Prato.builder()
                .categoria(categoria)
                .nome(request.nome())
                .descricao(request.descricao())
                .fotoUrl(request.fotoUrl())
                .precoVenda(request.precoVenda())
                .tempoPreparoMin(request.tempoPreparoMin())
                .status(StatusPrato.INATIVO) // RN01: nasce inativo até ter ficha técnica
                .build();

        return PratoResponse.from(pratoRepository.save(prato));
    }

    @Transactional
    public PratoResponse atualizar(Long id, PratoRequest request) {
        Prato prato = buscarEntidade(id);
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId());

        prato.setCategoria(categoria);
        prato.setNome(request.nome());
        prato.setDescricao(request.descricao());
        prato.setFotoUrl(request.fotoUrl());
        prato.setPrecoVenda(request.precoVenda());
        prato.setTempoPreparoMin(request.tempoPreparoMin());

        if (request.status() != null) {
            aplicarMudancaStatus(prato, StatusPrato.valueOf(request.status().toUpperCase()));
        }

        return PratoResponse.from(pratoRepository.save(prato));
    }

    /** RN01: prato só pode ficar ATIVO se tiver ficha técnica com pelo menos 1 ingrediente. */
    @Transactional
    public PratoResponse mudarStatus(Long id, String novoStatus) {
        Prato prato = buscarEntidade(id);
        aplicarMudancaStatus(prato, StatusPrato.valueOf(novoStatus.toUpperCase()));
        return PratoResponse.from(pratoRepository.save(prato));
    }

    private void aplicarMudancaStatus(Prato prato, StatusPrato novoStatus) {
        if (novoStatus == StatusPrato.ATIVO) {
            FichaTecnica ficha = fichaTecnicaRepository.findByPratoId(prato.getId()).orElse(null);
            if (ficha == null || ficha.getItens().isEmpty()) {
                throw new BusinessException("RN01: o prato só pode ficar ATIVO se tiver ficha técnica com pelo menos 1 ingrediente.");
            }
        }
        prato.setStatus(novoStatus);
    }

    /** RN06: exclusão lógica. */
    @Transactional
    public void desativar(Long id) {
        Prato prato = buscarEntidade(id);
        prato.setStatus(StatusPrato.INATIVO);
        pratoRepository.save(prato);
    }
}
