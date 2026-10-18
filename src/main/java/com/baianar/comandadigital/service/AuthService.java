package com.baianar.comandadigital.service;

import com.baianar.comandadigital.dto.request.LoginRequest;
import com.baianar.comandadigital.dto.request.RegisterRequest;
import com.baianar.comandadigital.dto.response.AuthResponse;
import com.baianar.comandadigital.entity.Usuario;
import com.baianar.comandadigital.entity.enums.Perfil;
import com.baianar.comandadigital.entity.enums.StatusUsuario;
import com.baianar.comandadigital.exception.ConflictException;
import com.baianar.comandadigital.repository.UsuarioRepository;
import com.baianar.comandadigital.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Infraestrutura compartilhada de autenticação (RF-038 a RF-040).
 * O login serve TODOS os perfis (ADMIN/GERENTE/COZINHEIRO/CLIENTE);
 * o cadastro público (register) sempre cria um usuário CLIENTE (RF-039).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."));

        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getEmail(), usuario.getPerfil().name());

        return new AuthResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name(),
                usuario.getTelefone(), usuario.getEndereco());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // RN10: email único
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("Já existe uma conta cadastrada com este email.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.CLIENTE)
                .telefone(request.telefone())
                .endereco(request.endereco())
                .status(StatusUsuario.ATIVO)
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generateToken(usuario.getId(), usuario.getEmail(), usuario.getPerfil().name());

        return new AuthResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name(),
                usuario.getTelefone(), usuario.getEndereco());
    }
}
