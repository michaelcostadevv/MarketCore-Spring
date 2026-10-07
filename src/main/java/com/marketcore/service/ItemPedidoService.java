package com.marketcore.service;

import com.marketcore.entidades.ItemPedido;
import com.marketcore.entidades.Pedido;
import com.marketcore.repository.ItemPedidoRepository;
import com.marketcore.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemPedidoService {

    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<ItemPedido> listarItens() {
        return itemPedidoRepository.findAll();
    }

    public ItemPedido adicionarItem(ItemPedido itemPedido) {
        // Valida se o pedido associado existe
        if (itemPedido.getPedido() == null || itemPedido.getPedido().getId() == null) {
            throw new RuntimeException("O item precisa estar vinculado a um pedido válido.");
        }

        Pedido pedido = pedidoRepository.findById(itemPedido.getPedido().getId())
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID informado."));

        // Gera um ID único para o item se ele não vier preenchido
        if (itemPedido.getId() == null || itemPedido.getId().isEmpty()) {
            itemPedido.setId(UUID.randomUUID().toString());
        }

        itemPedido.setPedido(pedido);

        return itemPedidoRepository.save(itemPedido);
    }
}
