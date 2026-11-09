package com.marketcore.exception;

import java.time.Instant;
import java.util.List;

/** Formato único de erro devolvido por toda a API. */
public record ErroResposta(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<String> detalhes
) {
    public ErroResposta(int status, String erro, String mensagem, String caminho) {
        this(Instant.now(), status, erro, mensagem, caminho, List.of());
    }

    public ErroResposta(int status, String erro, String mensagem, String caminho, List<String> detalhes) {
        this(Instant.now(), status, erro, mensagem, caminho, detalhes);
    }
}
