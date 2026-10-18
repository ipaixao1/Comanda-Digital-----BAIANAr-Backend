package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.Prato;
import com.baianar.comandadigital.entity.enums.StatusPrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PratoRepository extends JpaRepository<Prato, Long> {
    Page<Prato> findByStatus(StatusPrato status, Pageable pageable);
    Page<Prato> findByStatusAndCategoriaId(StatusPrato status, Long categoriaId, Pageable pageable);
    Page<Prato> findAllByOrderByIdAsc(Pageable pageable);
}
