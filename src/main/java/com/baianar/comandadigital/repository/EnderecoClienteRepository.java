package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.EnderecoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnderecoClienteRepository extends JpaRepository<EnderecoCliente, Long> {

    /** Padrão primeiro; depois do mais antigo para o mais novo. */
    List<EnderecoCliente> findByClienteIdOrderByPadraoDescIdAsc(Long clienteId);

    Optional<EnderecoCliente> findByIdAndClienteId(Long id, Long clienteId);

    long countByClienteId(Long clienteId);

    Optional<EnderecoCliente> findFirstByClienteIdOrderByIdDesc(Long clienteId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE EnderecoCliente e SET e.padrao = false WHERE e.cliente.id = :clienteId")
    void limparPadrao(@Param("clienteId") Long clienteId);
}
