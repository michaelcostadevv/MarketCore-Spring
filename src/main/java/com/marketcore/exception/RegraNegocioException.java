package com.marketcore.exception;

/**
 * Lançada quando uma requisição é válida no formato, mas viola uma regra de negócio
 * (ex.: estoque insuficiente, transição de status inválida). Resulta em HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
