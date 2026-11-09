package com.marketcore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequestDTO(
        @NotNull(message = "O ID do produto é obrigatório")
        Long produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade de um item deve ser no mínimo 1")
        @Max(value = 1000, message = "A quantidade de um item deve ser no máximo 1000")
        Integer quantidade
) {}
