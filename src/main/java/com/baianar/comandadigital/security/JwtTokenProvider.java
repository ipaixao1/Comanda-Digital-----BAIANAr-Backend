package com.baianar.comandadigital.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * RNF02: JWT com expiração (8h), payload contendo o perfil (role) do usuário.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Garante >= 256 bits para HS256, independente do tamanho do segredo configurado.
        byte[] rawKey = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(rawKey.length >= 32 ? rawKey : Decoders.BASE64.decode(padTo32(secret)));
    }

    private String padTo32(String raw) {
        StringBuilder sb = new StringBuilder(raw);
        while (sb.toString().getBytes(StandardCharsets.UTF_8).length < 32) {
            sb.append(raw);
        }
        return java.util.Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String email, String perfil) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("perfil", perfil)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return parseClaims(token).get("uid", Long.class);
    }

    public String getPerfilFromToken(String token) {
        return parseClaims(token).get("perfil", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
