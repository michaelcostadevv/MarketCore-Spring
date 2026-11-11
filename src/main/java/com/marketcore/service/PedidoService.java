package com.marketcore.service;

import com.marketcore.dto.ItemPedidoRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entity.Cliente;
import com.marketcore.entity.ItemPedido;
import com.marketcore.entity.Pedido;
import com.marketcore.entity.Produto;
import com.marketcore.entity.User;
import com.marketcore.enums.StatusPedido;
import com.marketcore.exception.RecursoNaoEncontradoException;
import com.marketcore.exception.RegraNegocioException;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.PedidoRepository;
import com.marketcore.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de acesso (autorização por objeto):
 * - ADMIN vê e altera qualquer pedido.
 * - Cliente só enxerga os PRÓPRIOS pedidos e só pode cancelá-los.
 * - Pedido de outra pessoa responde 404 (e não 403), para não revelar que ele existe.
 */
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
     * Cria o pedido para o cliente do usuário logado e baixa o estoque na mesma transação:
     * se qualquer item falhar (produto inexistente, estoque insuficiente), nada é gravado.
     */
    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto, User usuario) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Sua conta não possui cadastro de cliente para realizar pedidos"));

        Pedido pedido = new Pedido(cliente);

        for (ItemPedidoRequestDTO itemDto : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", itemDto.produtoId()));

            produto.baixarEstoque(itemDto.quantidade());
            pedido.adicionarItem(new ItemPedido(produto, itemDto.quantidade()));
        }

        return PedidoResponseDTO.de(pedidoRepository.save(pedido));
    }

    public PedidoResponseDTO buscarPorId(Long id, User usuario) {
        return PedidoResponseDTO.de(buscarVisivel(id, usuario));
    }

    /** ADMIN: todos os pedidos. Cliente: apenas os seus. Filtro de status opcional nos dois casos. */
    public Page<PedidoResponseDTO> listar(StatusPedido status, User usuario, Pageable pageable) {
        Page<Pedido> pedidos;
        if (isAdmin(usuario)) {
            pedidos = (status == null)
                    ? pedidoRepository.findAll(pageable)
                    : pedidoRepository.findByStatusPedido(status, pageable);
        } else {
            pedidos = (status == null)
                    ? pedidoRepository.findByClienteUsuarioId(usuario.getId(), pageable)
                    : pedidoRepository.findByClienteUsuarioIdAndStatusPedido(usuario.getId(), status, pageable);
        }
        return pedidos.map(PedidoResponseDTO::de);
    }

    /**
     * ADMIN aplica qualquer transição válida. O cliente só pode CANCELAR o próprio pedido.
     * Cancelar devolve os itens ao estoque (ver Pedido.alterarStatus).
     */
    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus, User usuario) {
        Pedido pedido = buscarVisivel(id, usuario); // 404 se não for dele

        if (!isAdmin(usuario) && novoStatus != StatusPedido.CANCELADO) {
            // Aqui sim 403: o pedido é dele, então não há o que esconder; ele só não tem permissão
            throw new AccessDeniedException("Clientes só podem cancelar pedidos");
        }

        pedido.alterarStatus(novoStatus);
        return PedidoResponseDTO.de(pedido);
    }

    /** Busca o pedido respeitando o dono: para quem não é admin, a query já filtra pelo usuário. */
    private Pedido buscarVisivel(Long id, User usuario) {
        return (isAdmin(usuario)
                ? pedidoRepository.findById(id)
                : pedidoRepository.findByIdAndClienteUsuarioId(id, usuario.getId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido", id));
    }

    private static boolean isAdmin(User usuario) {
        return AuthService.ROLE_ADMIN.equals(usuario.getRole());
    }
}
