package com.marketcore.dto;

import com.marketcore.entity.Produto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        BigDecimal preco,
        Integer quantidade,
        String descricao,
        String imagemUrl
) {
    public static ProdutoResponseDTO de(Produto p) {
        return new ProdutoResponseDTO(p.getId(), p.getNome(), p.getPreco(), p.getQuantidade(),
                p.getDescricao(), p.getImagemUrl());
    }
}
