package com.marketcore.service;

import com.marketcore.dto.ItemPedidoRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entity.Cliente;
import com.marketcore.entity.ItemPedido;
import com.marketcore.entity.Pedido;
import com.marketcore.entity.Produto;
import com.marketcore.enums.StatusPedido;
import com.marketcore.exception.RecursoNaoEncontradoException;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.PedidoRepository;
import com.marketcore.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Cria o pedido e baixa o estoque na mesma transação: se qualquer item falhar
     * (produto inexistente, estoque insuficiente), nada é gravado.
     */
    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", dto.clienteId()));

        Pedido pedido = new Pedido(cliente);

        for (ItemPedidoRequestDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", itemDto.produtoId()));

            produto.baixarEstoque(itemDto.quantidade());
            pedido.adicionarItem(new ItemPedido(produto, itemDto.quantidade()));
        }

        return PedidoResponseDTO.de(pedidoRepository.save(pedido));
    }

    public PedidoResponseDTO buscarPorId(Long id) {
        return PedidoResponseDTO.de(buscarEntidade(id));
    }

    public Page<PedidoResponseDTO> listar(StatusPedido status, Pageable pageable) {
        Page<Pedido> pedidos = (status == null)
                ? pedidoRepository.findAll(pageable)
                : pedidoRepository.findByStatusPedido(status, pageable);
        return pedidos.map(PedidoResponseDTO::de);
    }

    /** Muda o status respeitando o ciclo de vida; cancelar devolve os itens ao estoque. */
    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = buscarEntidade(id);
        pedido.alterarStatus(novoStatus);
        return PedidoResponseDTO.de(pedido);
    }

    private Pedido buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }
}
