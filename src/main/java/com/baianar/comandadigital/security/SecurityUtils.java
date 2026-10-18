package com.baianar.comandadigital.security;

import org.springframework.security.core.context.SecurityContextHolder;

/** Pequeno helper para obter o usuário autenticado atual a partir do SecurityContext. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Usuário autenticado não encontrado no contexto de segurança.");
    }

    public static String getCurrentUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getEmail();
        }
        throw new IllegalStateException("Usuário autenticado não encontrado no contexto de segurança.");
    }
}
