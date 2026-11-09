package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.Ingrediente;
import com.baianar.comandadigital.entity.enums.StatusIngrediente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {
    Page<Ingrediente> findAll(Pageable pageable);
    boolean existsBySku(String sku);
    Optional<Ingrediente> findBySku(String sku);
    List<Ingrediente> findAllByIdIn(List<Long> ids);
    List<Ingrediente> findByStatusOrderByNomeAsc(StatusIngrediente status);
}
