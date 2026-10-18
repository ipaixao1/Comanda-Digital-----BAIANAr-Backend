package com.baianar.comandadigital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ComandaDigitalApplication {

    public static void main(String[] args) {
        // Datas/horas (LocalDateTime) são gravadas e devolvidas em UTC; o front converte para o fuso do usuário.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ComandaDigitalApplication.class, args);
    }
}
