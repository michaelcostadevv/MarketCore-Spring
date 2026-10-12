package com.marketcore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProdutoRequestDTO {

    // @NotBlank garante que o texto não é nulo e não está vazio (não aceita só espaços)
    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    // @Min(0) garante que o sistema não aceita números negativos
    @NotNull(message = "A quantidade é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    private Integer quantidade;

    @NotNull(message = "O preço é obrigatório")
    @Min(value = 0, message = "O preço não pode ser negativo")
    private Double preco;

    // Construtores, Getters e Setters
    public ProdutoRequestDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }
}