package com.baianar.comandadigital.exception;

/** 409 — conflito (ex.: RN10 email duplicado). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
