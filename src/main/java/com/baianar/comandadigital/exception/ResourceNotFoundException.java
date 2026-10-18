package com.baianar.comandadigital.exception;

/** 404 — recurso não encontrado. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
