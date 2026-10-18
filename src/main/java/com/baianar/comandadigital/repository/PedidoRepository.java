package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.Pedido;
import com.baianar.comandadigital.entity.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Pedido> findByStatusOrderByCreatedAtDesc(StatusPedido status, Pageable pageable);

    Page<Pedido> findByClienteIdOrderByCreatedAtDesc(Long clienteId, Pageable pageable);

    Page<Pedido> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.valorTotal), 0) FROM Pedido p WHERE p.status <> 'CANCELADO' AND p.createdAt BETWEEN :inicio AND :fim")
    BigDecimal somaFaturamento(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status <> 'CANCELADO' AND p.createdAt BETWEEN :inicio AND :fim")
    Long contaPedidos(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT p FROM Pedido p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:inicio IS NULL OR p.createdAt >= :inicio)
            AND (:fim IS NULL OR p.createdAt <= :fim)
            ORDER BY p.createdAt DESC
            """)
    Page<Pedido> buscarComFiltros(@Param("status") StatusPedido status,
                                   @Param("inicio") LocalDateTime inicio,
                                   @Param("fim") LocalDateTime fim,
                                   Pageable pageable);

    @Query("""
            SELECT pi.prato.id as pratoId, pi.prato.nome as nome, SUM(pi.quantidade) as totalVendido
            FROM PedidoItem pi
            WHERE pi.pedido.status <> 'CANCELADO' AND pi.pedido.createdAt BETWEEN :inicio AND :fim
            GROUP BY pi.prato.id, pi.prato.nome
            ORDER BY SUM(pi.quantidade) DESC
            """)
    List<Object[]> topPratosVendidos(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, Pageable pageable);

    @Query("""
            SELECT FUNCTION('DATE', p.createdAt) as dia, COALESCE(SUM(p.valorTotal), 0) as total
            FROM Pedido p
            WHERE p.status <> 'CANCELADO' AND p.createdAt BETWEEN :inicio AND :fim
            GROUP BY FUNCTION('DATE', p.createdAt)
            ORDER BY FUNCTION('DATE', p.createdAt)
            """)
    List<Object[]> vendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
