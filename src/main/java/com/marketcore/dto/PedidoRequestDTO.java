package com.marketcore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Não existe "clienteId" aqui de propósito: o dono do pedido vem do TOKEN (usuário logado).
 * Se viesse do corpo, qualquer usuário poderia fazer pedidos em nome de outro.
 */
public record PedidoRequestDTO(
        @NotEmpty(message = "O pedido deve ter pelo menos um item")
        @Size(max = 100, message = "O pedido pode ter no máximo 100 itens")
        List<@Valid @NotNull ItemPedidoRequestDTO> itens
) {}
