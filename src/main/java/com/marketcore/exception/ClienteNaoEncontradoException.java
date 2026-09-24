package com.marketcore.exception;

public class ClienteNaoEncontradoException extends RuntimeException{
    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
