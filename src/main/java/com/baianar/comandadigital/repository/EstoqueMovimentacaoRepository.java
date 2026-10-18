package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.EstoqueMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface EstoqueMovimentacaoRepository extends JpaRepository<EstoqueMovimentacao, Long> {

    Page<EstoqueMovimentacao> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<EstoqueMovimentacao> findByIngredienteIdOrderByCreatedAtDesc(Long ingredienteId, Pageable pageable);

    /** RF-026: histórico de preços de compra de um ingrediente, para gráfico de evolução. */
    @Query("""
            SELECT m FROM EstoqueMovimentacao m
            WHERE m.ingrediente.id = :ingredienteId AND m.motivo = 'COMPRA'
            ORDER BY m.createdAt ASC
            """)
    java.util.List<EstoqueMovimentacao> historicoPrecosCompra(@Param("ingredienteId") Long ingredienteId);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN m.tipo = 'ENTRADA' THEN m.quantidade
                                      WHEN m.tipo = 'ESTORNO' THEN m.quantidade
                                      ELSE -m.quantidade END), 0)
            FROM EstoqueMovimentacao m
            WHERE m.ingrediente.id = :ingredienteId
            """)
    BigDecimal saldoPorIngrediente(@Param("ingredienteId") Long ingredienteId);
}
