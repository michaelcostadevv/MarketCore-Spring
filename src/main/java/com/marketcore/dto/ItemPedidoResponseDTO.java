package com.marketcore.dto;

public class ItemPedidoResponseDTO {

    private String nomeProduto;
    private Integer quantidade;
    private Double precoUnitario;

    // Opcional, mas muito útil para o frontend:
    private Double subTotal;

    public ItemPedidoResponseDTO(String nome, Integer quantidade, Double precoUnitario, Double subTotal) {}

    public ItemPedidoResponseDTO(String nomeProduto, Integer quantidade, Double precoUnitario) {
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subTotal = quantidade * precoUnitario;
    }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(Double precoUnitario) { this.precoUnitario = precoUnitario; }

    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }
}