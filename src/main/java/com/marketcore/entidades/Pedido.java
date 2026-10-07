package com.marketcore.entidades;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table (name = "pedido")
public class Pedido {
    @Id
    private String id;

    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Column(name = "status_pedido", nullable = false)
    private String statusPedido;

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido;

    @ManyToOne // Informa ao Spring que vários pedidos podem ser feitos por um único cliente.
    @JoinColumn(name = "cliente_id", nullable = false)  // @JoinColumn(name = "cliente_id"): Mapeia exatamente a coluna de chave estrangeira que criamos no MySQL.
    private Cliente cliente;

    // Construtor vazio (obrigatório para o JPA)
    public Pedido() {
    }

    // Construtor com parâmetros
    public Pedido(String id, Double valorTotal, String statusPedido, LocalDateTime dataPedido, Cliente cliente) {
        this.id = id;
        this.valorTotal = valorTotal;
        this.statusPedido = statusPedido;
        this.dataPedido = dataPedido;
        this.cliente = cliente;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(String statusPedido) {
        this.statusPedido = statusPedido;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
