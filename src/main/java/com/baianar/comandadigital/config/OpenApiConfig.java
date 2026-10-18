package com.baianar.comandadigital.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** RNF11: Swagger acessível em /swagger-ui.html, com suporte a JWT (botão Authorize). */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Comanda Digital · BAIANAr API",
                version = "1.0.0",
                description = "Backend do sistema de pedidos para dark kitchen BAIANAr (SRS v3.2)."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
