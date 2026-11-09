package com.marketcore.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    /**
     * A chave vem da propriedade jwt.secret (variável de ambiente JWT_SECRET) e nunca fica no código.
     * Keys.hmacShaKeyFor falha na inicialização se a chave tiver menos de 256 bits (32 bytes).
     */
    public JwtService(@Value("${jwt.secret}") String segredo,
                      @Value("${jwt.expiration-ms}") long expiracaoMs) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    /**
     * Valida assinatura e expiração e devolve o e-mail (subject) do token.
     *
     * @throws JwtException se o token for inválido, adulterado ou estiver expirado
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token) // já rejeita tokens expirados
                .getPayload();
        return claims.getSubject();
    }
}
