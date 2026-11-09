package com.marketcore.entity;

import com.marketcore.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    /**
     * Lock otimista: se dois pedidos alterarem o estoque do mesmo produto ao mesmo tempo,
     * o segundo falha (409) em vez de sobrescrever o primeiro e vender além do estoque.
     */
    @Version
    private long version;

    public Produto() {}

    public Produto(String nome, Integer quantidade, BigDecimal preco) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public void baixarEstoque(int qtd) {
        if (quantidade < qtd) {
            throw new RegraNegocioException("Estoque insuficiente para o produto '" + nome
                    + "'. Disponível: " + quantidade + ", solicitado: " + qtd);
        }
        quantidade -= qtd;
    }

    public void reporEstoque(int qtd) {
        quantidade += qtd;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public long getVersion() { return version; }
}
