package com.marketcore.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * CORS: diz ao NAVEGADOR quais origens (protocolo + domínio + porta) podem chamar esta API.
 *
 * <p>Não é uma proteção da API (Postman e curl ignoram CORS); protege o usuário do navegador
 * contra sites de terceiros usando a API em seu nome. A autenticação continua sendo o JWT.
 */
@Configuration
public class CorsConfig {

    /**
     * @param origens lista separada por vírgula, vinda de CORS_ALLOWED_ORIGINS.
     *                Padrão: o servidor de desenvolvimento do Vite.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String origens) {

        CorsConfiguration config = new CorsConfiguration();
        // Origens explícitas, nunca "*": só o nosso frontend pode chamar a API pelo navegador
        config.setAllowedOrigins(Arrays.stream(origens.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Sem isso o JavaScript do front não consegue ler o cabeçalho Location dos POSTs (201)
        config.setExposedHeaders(List.of("Location"));
        // O navegador guarda a resposta do preflight por 1h e não repete o OPTIONS a cada chamada
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
