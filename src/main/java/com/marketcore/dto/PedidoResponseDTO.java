package com.marketcore.dto;

import com.marketcore.entity.Pedido;
import com.marketcore.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        String nomeCliente,
        BigDecimal valorTotal,
        StatusPedido statusPedido,
        Instant dataPedido, // sai no JSON em UTC, com "Z" (ex.: 2026-11-11T10:45:00Z)
        List<ItemPedidoResponseDTO> itens
) {
    public static PedidoResponseDTO de(Pedido p) {
        return new PedidoResponseDTO(
                p.getId(),
                p.getCliente().getId(),
                p.getCliente().getNome(),
                p.getValorTotal(),
                p.getStatusPedido(),
                p.getDataPedido(),
                p.getItens().stream().map(ItemPedidoResponseDTO::de).toList());
    }
}
