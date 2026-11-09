package com.marketcore.entity;

import com.marketcore.enums.StatusPedido;
import com.marketcore.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pedido", nullable = false)
    private StatusPedido statusPedido = StatusPedido.PENDENTE;

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {} // exigido pelo JPA

    public Pedido(Cliente cliente) {
        this.cliente = cliente;
    }

    /** Adiciona o item, mantendo os dois lados da relação e o valor total sempre consistentes. */
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        itens.add(item);
        valorTotal = valorTotal.add(item.getSubTotal());
    }

    /** Aplica a transição de status validando o ciclo de vida definido em {@link StatusPedido}. */
    public void alterarStatus(StatusPedido novoStatus) {
        if (!statusPedido.podeMudarPara(novoStatus)) {
            throw new RegraNegocioException("Não é possível mudar o pedido de " + statusPedido + " para " + novoStatus
                    + ". Transições permitidas: " + statusPedido.proximosPermitidos());
        }
        if (novoStatus == StatusPedido.CANCELADO) {
            itens.forEach(item -> item.getProduto().reporEstoque(item.getQuantidade()));
        }
        statusPedido = novoStatus;
    }

    public Long getId() { return id; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public LocalDateTime getDataPedido() { return dataPedido; }
    public Cliente getCliente() { return cliente; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public StatusPedido getStatusPedido() { return statusPedido; }
}
