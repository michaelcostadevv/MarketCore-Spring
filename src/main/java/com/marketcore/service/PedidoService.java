package com.marketcore.service;

import com.marketcore.dto.ItemPedidoRequestDTO;
import com.marketcore.dto.ItemPedidoResponseDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entidades.Cliente;
import com.marketcore.entidades.ItemPedido;
import com.marketcore.entidades.Pedido;
import com.marketcore.entidades.Produto;
import com.marketcore.enums.StatusPedido;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.PedidoRepository;
import com.marketcore.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO requestDTO) {

        // 1. Busca o cliente real no banco de dados
        Cliente cliente = clienteRepository.findById(requestDTO.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + requestDTO.getClienteId()));

        // 2. Prepara o Pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatusPedido(StatusPedido.PENDENTE); // Sempre começa como pendente

        List<ItemPedido> itens = new ArrayList<>();
        double valorTotalPedido = 0.0;

        // 3. Processa cada item do pedido
        for (ItemPedidoRequestDTO itemDto : requestDTO.getItens()) {

            // Busca o produto real
            Produto produto = produtoRepository.findById(itemDto.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + itemDto.getProdutoId()));

            // 4. Valida e Baixa o Estoque
            if (produto.getQuantidade() < itemDto.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getNome() +
                        ". Quantidade disponível: " + produto.getQuantidade());
            }
            produto.setQuantidade(produto.getQuantidade() - itemDto.getQuantidade());
            produtoRepository.save(produto); // Salva a baixa no estoque

            // 5. Cria o ItemPedido
            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(produto.getPreco()); // Pega o preço do banco de dados (segurança)
            item.setSubTotal(produto.getPreco() * itemDto.getQuantidade());
            item.setPedido(pedido); // Conecta o item ao pedido

            itens.add(item);
            valorTotalPedido += item.getSubTotal();
        }

        pedido.setItens(itens);
        pedido.setValorTotal(valorTotalPedido);

        // 6. Salva o pedido completo (com os itens juntos por causa do CascadeType.ALL)
        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 7. Converte a Entidade salva para ResponseDTO
        return converterParaResponseDTO(pedidoSalvo);
    }

    // --- NOVO MÉTODO: ATUALIZAR STATUS DO PEDIDO ---
    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus) {
        // Busca o pedido no banco
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));

        // Regra de Negócio Exemplo: Não pode enviar um pedido que foi cancelado
        if (pedido.getStatusPedido() == StatusPedido.CANCELADO && novoStatus != StatusPedido.CANCELADO) {
            throw new RuntimeException("Não é possível alterar o status de um pedido que já foi CANCELADO.");
        }

        // Atualiza o status
        pedido.setStatusPedido(novoStatus);

        // Salva a alteração
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        // Retorna o DTO atualizado
        return converterParaResponseDTO(pedidoAtualizado);
    }

    // Método auxiliar para evitar repetição de código
    private PedidoResponseDTO converterParaResponseDTO(Pedido pedido) {
        List<ItemPedidoResponseDTO> itensDTO = new ArrayList<>();

        for (ItemPedido item : pedido.getItens()) {
            itensDTO.add(new ItemPedidoResponseDTO(
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    item.getPrecoUnitario(),
                    item.getSubTotal()
            ));
        }

        return new PedidoResponseDTO(
                        pedido.getId(),
                        pedido.getCliente().getNome(),
                        pedido.getValorTotal(),
                itensDTO, // Converte o Enum para String no JSON
                pedido.getStatusPedido().name()
                );
    }
}