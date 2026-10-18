package com.baianar.comandadigital.exception;

/** 403 — operação não permitida pelas regras de negócio (ex.: RN04 cancelamento). */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
