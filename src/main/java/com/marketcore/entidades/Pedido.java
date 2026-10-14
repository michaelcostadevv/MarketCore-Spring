package com.marketcore.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "pedido")
public class Pedido {

    // 1. Corrigido para Long com Auto-Incremento para combinar com os DTOs
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Column(name = "status_pedido", nullable = false)
    private String statusPedido = "PENDENTE"; // Valor padrão bacana de se ter

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido = LocalDateTime.now(); // Já preenche com a data atual

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // 2. A MÁGICA AQUI: O mapeamento da lista de itens!
    // mappedBy = "pedido" diz que quem controla essa relação é a variável "pedido" lá na classe ItemPedido
    // cascade = CascadeType.ALL diz que se salvarmos o pedido, ele salva os itens junto automaticamente.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens = new ArrayList<>();

    // Construtor vazio (obrigatório para o JPA)
    public Pedido() {
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public String getStatusPedido() { return statusPedido; }
    public void setStatusPedido(String statusPedido) { this.statusPedido = statusPedido; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    // O método que estava faltando e causando o erro no Controller!
    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }
}