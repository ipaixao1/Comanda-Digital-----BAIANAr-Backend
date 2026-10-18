package com.baianar.comandadigital.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** RNF06: formato padronizado de erro devolvido pelo GlobalExceptionHandler. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
