package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.Prato;
import com.baianar.comandadigital.entity.enums.StatusPrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PratoRepository extends JpaRepository<Prato, Long> {
    Page<Prato> findByStatus(StatusPrato status, Pageable pageable);
    Page<Prato> findByStatusAndCategoriaId(StatusPrato status, Long categoriaId, Pageable pageable);
    Page<Prato> findAllByOrderByIdAsc(Pageable pageable);

    /** Tempos de preparo cadastrados dos pratos com o status informado (usado na faixa de entrega da loja). */
    @Query("SELECT p.tempoPreparoMin FROM Prato p WHERE p.status = :status AND p.tempoPreparoMin IS NOT NULL")
    List<Integer> temposDePreparoPorStatus(@Param("status") StatusPrato status);
}
