package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.FichaTecnica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, Long> {
    Optional<FichaTecnica> findByPratoId(Long pratoId);
}
