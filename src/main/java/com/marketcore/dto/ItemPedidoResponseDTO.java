package com.marketcore.dto;

import com.marketcore.entity.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponseDTO(
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subTotal
) {
    public static ItemPedidoResponseDTO de(ItemPedido item) {
        return new ItemPedidoResponseDTO(
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubTotal());
    }
}
