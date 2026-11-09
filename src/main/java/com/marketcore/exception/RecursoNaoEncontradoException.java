package com.marketcore.exception;

/** Lançada quando um recurso buscado por ID não existe. Resulta em HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " não encontrado com o ID: " + id);
    }
}
