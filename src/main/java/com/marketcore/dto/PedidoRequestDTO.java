package com.marketcore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoRequestDTO(
        @NotNull(message = "O ID do cliente é obrigatório")
        Long clienteId,

        @NotEmpty(message = "O pedido deve ter pelo menos um item")
        @Size(max = 100, message = "O pedido pode ter no máximo 100 itens")
        List<@Valid @NotNull ItemPedidoRequestDTO> itens
) {}
