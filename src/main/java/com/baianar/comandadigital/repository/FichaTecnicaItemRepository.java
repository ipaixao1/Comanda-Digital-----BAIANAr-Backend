package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.FichaTecnicaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FichaTecnicaItemRepository extends JpaRepository<FichaTecnicaItem, Long> {
    List<FichaTecnicaItem> findByFichaTecnicaId(Long fichaTecnicaId);
}
