package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.FornecedorProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FornecedorProdutoRepository extends JpaRepository<FornecedorProduto, Long> {
    List<FornecedorProduto> findByIngredienteIdOrderByPrecoAsc(Long ingredienteId);
    List<FornecedorProduto> findByFornecedorId(Long fornecedorId);
    Optional<FornecedorProduto> findByFornecedorIdAndIngredienteId(Long fornecedorId, Long ingredienteId);
}
