package com.marketcore.service;

import com.marketcore.dto.ItemPedidoRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.entidades.Cliente;
import com.marketcore.entidades.ItemPedido;
import com.marketcore.entidades.Pedido;
import com.marketcore.entidades.Produto;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.PedidoRepository;
import com.marketcore.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    // Injetamos os repositórios necessários para buscar os dados reais
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    // @Transactional garante que ou salva TUDO (pedido, itens e baixa de estoque) ou não salva NADA se der erro.
    @Transactional
    public Pedido cadastrarPedido(PedidoRequestDTO dto) {

        // 1. Buscar o Cliente no banco usando o ID que veio no DTO
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + dto.getClienteId()));

        // 2. Preparar a Entidade Pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setValorTotal(0.0); // Começa com zero e vamos somando no loop

        // Lista que vai guardar as entidades ItemPedido
        List<ItemPedido> itens = new ArrayList<>();

        // 3. Fazer um loop (for) em cada item que veio do Postman
        for (ItemPedidoRequestDTO itemDto : dto.getItens()) {

            // 3.1 Buscar o Produto real no banco
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado com o ID: " + itemDto.getProdutoId()));

            // 3.2 Verificar se há estoque suficiente (Regra de Negócio!)
            if (produto.getQuantidade() < itemDto.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            // 3.3 Subtrair o estoque e atualizar o produto
            produto.setQuantidade(produto.getQuantidade() - itemDto.getQuantidade());
            produtoRepository.save(produto);

            // 3.4 Criar a Entidade ItemPedido e preencher com os dados
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProduto(produto);
            itemPedido.setQuantidade(itemDto.getQuantidade());
            itemPedido.setPrecoUnitario(produto.getPreco()); // Congela o preço no momento da compra
            itemPedido.setPedido(pedido); // Liga o item ao pedido para não dar erro no banco

            // 3.5 Adicionar na lista e somar ao valor total do pedido
            itens.add(itemPedido);
            pedido.setValorTotal(pedido.getValorTotal() + (produto.getPreco() * itemDto.getQuantidade()));
        }

        // 4. Colocar a lista de itens preenchida dentro do pedido
        pedido.setItens(itens);

        // 5. Salvar o pedido (O banco de dados vai salvar os itens automaticamente junto com ele)
        return pedidoRepository.save(pedido);
    }

    // --- MÉTODOS DE BUSCA (Usados no GET) ---
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
    }
}