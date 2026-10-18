package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.UsuarioInternoRequest;
import com.baianar.comandadigital.dto.response.UsuarioResponse;
import com.baianar.comandadigital.entity.Usuario;
import com.baianar.comandadigital.entity.enums.Perfil;
import com.baianar.comandadigital.entity.enums.StatusUsuario;
import com.baianar.comandadigital.exception.BusinessException;
import com.baianar.comandadigital.exception.ConflictException;
import com.baianar.comandadigital.exception.ResourceNotFoundException;
import com.baianar.comandadigital.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** RF-041: ADMIN gerencia usuários internos (gerente/cozinheiro). RN10: email único. */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UsuarioResponse> listarInternos(Pageable pageable) {
        return usuarioRepository.findByPerfilIn(List.of(Perfil.ADMIN, Perfil.GERENTE, Perfil.COZINHEIRO), pageable)
                .map(UsuarioResponse::from);
    }

    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.from(buscarEntidade(id));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioInternoRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("Já existe um usuário cadastrado com este email.");
        }

        String senha = (request.senha() != null && !request.senha().isBlank())
                ? request.senha()
                : UUID.randomUUID().toString().substring(0, 8);

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(senha))
                .perfil(Perfil.valueOf(request.perfil().toUpperCase()))
                .telefone(request.telefone())
                .status(StatusUsuario.ATIVO)
                .build();

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioInternoRequest request) {
        Usuario usuario = buscarEntidade(id);

        if (usuario.getPerfil() == Perfil.CLIENTE) {
            throw new BusinessException("Este endpoint gerencia apenas usuários internos (ADMIN/GERENTE/COZINHEIRO).");
        }

        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("Já existe um usuário cadastrado com este email.");
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(Perfil.valueOf(request.perfil().toUpperCase()));
        usuario.setTelefone(request.telefone());

        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    /** RN06: exclusão lógica. */
    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarEntidade(id);
        usuario.setStatus(StatusUsuario.INATIVO);
        usuarioRepository.save(usuario);
    }
}
