package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.PedidoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    Page<PedidoCompra> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
