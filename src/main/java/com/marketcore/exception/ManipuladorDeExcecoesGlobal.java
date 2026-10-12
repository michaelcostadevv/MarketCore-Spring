package com.marketcore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// Esta anotação transforma a classe num interceptador global
@RestControllerAdvice
public class ManipuladorDeExcecoesGlobal {

    // Diz ao Spring: "Se acontecer qualquer RuntimeException, vem para este método"
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroPadraoExeception> tratarRegrasDeNegocio(RuntimeException e) {

        ErroPadraoExeception erro = new ErroPadraoExeception();
        erro.setStatus(HttpStatus.BAD_REQUEST.value()); // Muda de 500 para 400 (Bad Request)
        erro.setMensagem(e.getMessage()); // Pega exatamente a frase que você escreveu no Service
        erro.setDataHora(LocalDateTime.now()); // Marca a hora exata do erro

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroPadraoExeception> tratarErrosDeValidacao(MethodArgumentNotValidException e) {

        // Pega todos os erros dos campos e junta as mensagens numa frase só
        String mensagens = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErroPadraoExeception erro = new ErroPadraoExeception();
        erro.setStatus(HttpStatus.BAD_REQUEST.value());
        erro.setMensagem(mensagens);
        erro.setDataHora(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}
