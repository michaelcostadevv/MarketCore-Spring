package com.marketcore.dto;

import com.marketcore.enums.StatusPedido;

import java.util.List;

public class PedidoResponseDTO {

    private Long id;
    private String nomeCliente; // Achata o objeto Cliente
    private Double valorTotal;
    private List<ItemPedidoResponseDTO> itens; // Usa a lista de DTOs e não de Entidades
    private String statusPedido;

    public PedidoResponseDTO() {
    }

    public PedidoResponseDTO(Long id, String nomeCliente, Double valorTotal, List<ItemPedidoResponseDTO> itens, String statusPedido) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.valorTotal = valorTotal;
        this.itens = itens;
        this.statusPedido = statusPedido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemPedidoResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResponseDTO> itens) {
        this.itens = itens;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }
}