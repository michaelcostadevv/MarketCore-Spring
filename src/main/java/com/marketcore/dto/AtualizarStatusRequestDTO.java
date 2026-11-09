package com.marketcore.dto;

import com.marketcore.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequestDTO(
        @NotNull(message = "O novo status é obrigatório")
        StatusPedido status
) {}
