package com.marketcore.exception;

public class ProdutoNaoEncontradoException extends RuntimeException {
    public ProdutoNaoEncontradoException(String messagem) {
        super(messagem);
    }
}
