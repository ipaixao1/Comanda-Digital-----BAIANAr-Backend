package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.CategoriaRequest;
import com.baianar.comandadigital.dto.response.CategoriaResponse;
import com.baianar.comandadigital.entity.Categoria;
import com.baianar.comandadigital.entity.enums.StatusCategoria;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF-009: CRUD categorias (criar, editar, listar, desativar — RN06: exclusão lógica). */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public Page<CategoriaResponse> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable).map(CategoriaResponse::from);
    }

    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.from(buscarEntidade(id));
    }

    Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = Categoria.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .ordem(request.ordem() != null ? request.ordem() : 0)
                .status(StatusCategoria.ATIVO)
                .build();
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        if (request.ordem() != null) {
            categoria.setOrdem(request.ordem());
        }
        if (request.status() != null) {
            categoria.setStatus(request.status());
       }
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    /** RN06: soft delete — nunca remove fisicamente. */
    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscarEntidade(id);
        categoria.setStatus(StatusCategoria.INATIVO);
        categoriaRepository.save(categoria);
    }
}
