package com.marketcore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class PedidoRequestDTO {

    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;

    // @NotEmpty garante que a lista não é nula e tem pelo menos 1 item
    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    @Valid // O @Valid aqui manda o Spring validar as regras lá do ItemPedidoRequestDTO também!
    private List<ItemPedidoRequestDTO> itens;

    public PedidoRequestDTO() {}

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public List<ItemPedidoRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoRequestDTO> itens) { this.itens = itens; }
}