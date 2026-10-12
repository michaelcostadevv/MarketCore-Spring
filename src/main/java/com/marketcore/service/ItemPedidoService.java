package com.marketcore.service;

import com.marketcore.entidades.ItemPedido;
import com.marketcore.entidades.Pedido;
import com.marketcore.entidades.Produto;
import com.marketcore.repository.ItemPedidoRepository;
import com.marketcore.repository.PedidoRepository;
import com.marketcore.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante para regras de negócio

import java.util.List;
import java.util.UUID;

@Service
public class ItemPedidoService{

        @Autowired
        private ItemPedidoRepository itemPedidoRepository;

        @Autowired
        private PedidoRepository pedidoRepository;

        @Autowired
        private ProdutoRepository produtoRepository; // Injetamos o repositório de produto

        public List<ItemPedido> listarItens() {
            return itemPedidoRepository.findAll();
        }

        // A anotação @Transactional garante que se der erro no meio, ele desfaz tudo (Rollback)
        @Transactional
        public ItemPedido adicionarItem(ItemPedido itemPedido) {

            // 1. Valida e busca o Pedido
            if (itemPedido.getPedido() == null || itemPedido.getPedido().getId() == null) {
                throw new RuntimeException("O item precisa estar vinculado a um pedido válido.");
            }
            Pedido pedido = pedidoRepository.findById(itemPedido.getPedido().getId())
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID informado."));

            // 2. Busca o Produto real no banco de dados
            Produto produto = produtoRepository.findById(itemPedido.getProduto().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

            // 3. Regra de Negócio: Verifica o estoque
            // Atenção: Troque "getEstoque" para o nome exato do seu atributo (ex: getQuantidadeEstoque)
            if (produto.getQuantidade() < itemPedido.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome()
                        + ". Estoque atual: " + produto.getQuantidade());
            }

            // 4. Subtrai o estoque e salva o produto atualizado
            produto.setQuantidade((produto.getQuantidade()) - itemPedido.getQuantidade());
            produtoRepository.save(produto);

            // 5. Gera ID e salva o Item do Pedido
            if (itemPedido.getId() == null || itemPedido.getId().isEmpty()) {
                itemPedido.setId(UUID.randomUUID().toString());
            }
            itemPedido.setPedido(pedido);

            return itemPedidoRepository.save(itemPedido);
        }
    }