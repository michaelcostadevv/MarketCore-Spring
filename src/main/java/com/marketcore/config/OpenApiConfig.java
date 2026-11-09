package com.marketcore.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita o botão "Authorize" no Swagger UI: faça login em /auth/login,
 * copie o token e cole lá para testar as rotas protegidas.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "MarketCore API", version = "1.0",
                description = "API de e-commerce: clientes, produtos, pedidos e controle de estoque"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
