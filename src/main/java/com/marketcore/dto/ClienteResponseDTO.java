package com.marketcore.dto;

import com.marketcore.entity.Cliente;

public record ClienteResponseDTO(Long id, String nome, String email) {

    public static ClienteResponseDTO de(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getNome(), c.getEmail());
    }
}
