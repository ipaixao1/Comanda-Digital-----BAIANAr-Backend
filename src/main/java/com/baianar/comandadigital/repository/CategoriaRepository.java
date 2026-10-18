package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.Categoria;
import com.baianar.comandadigital.entity.enums.StatusCategoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Page<Categoria> findByStatus(StatusCategoria status, Pageable pageable);
    List<Categoria> findByStatusOrderByOrdemAsc(StatusCategoria status);
}
