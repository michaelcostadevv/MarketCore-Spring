package com.marketcore.exception;

/** Lançada quando a operação conflita com o estado atual (ex.: e-mail já cadastrado). Resulta em HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
