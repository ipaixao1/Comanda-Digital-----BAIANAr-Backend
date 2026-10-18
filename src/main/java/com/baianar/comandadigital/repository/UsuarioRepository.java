package com.baianar.comandadigital.repository;

import com.baianar.comandadigital.entity.Usuario;
import com.baianar.comandadigital.entity.enums.Perfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Usuario> findByPerfilIn(java.util.List<Perfil> perfis, Pageable pageable);
}
