package com.marketcore.dto;

/**
 * Resposta do GET /auth/me: o que o frontend precisa saber sobre quem está logado.
 *
 * @param clienteId e nome ficam null para usuários sem cadastro de cliente (ex.: o admin)
 * @param admin     atalho para o front decidir se mostra o painel administrativo
 */
public record UsuarioLogadoDTO(
        Long id,
        String email,
        String role,
        boolean admin,
        Long clienteId,
        String nome
) {}
