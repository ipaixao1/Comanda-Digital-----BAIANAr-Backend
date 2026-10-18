package com.baianar.comandadigital.exception;

/** 422 — regra de negócio violada (ex.: RN03 estoque insuficiente). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
