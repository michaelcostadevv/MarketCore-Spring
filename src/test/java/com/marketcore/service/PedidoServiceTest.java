package com.marketcore.service;

import com.marketcore.dto.ItemPedidoRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entity.Cliente;
import com.marketcore.entity.Pedido;
import com.marketcore.entity.Produto;
import com.marketcore.entity.User;
import com.marketcore.enums.StatusPedido;
import com.marketcore.exception.RecursoNaoEncontradoException;
import com.marketcore.exception.RegraNegocioException;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.PedidoRepository;
import com.marketcore.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock PedidoRepository pedidoRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock ProdutoRepository produtoRepository;
    @InjectMocks PedidoService pedidoService;

    private final User ana = usuario(1L, AuthService.ROLE_PADRAO);
    private final User admin = usuario(99L, AuthService.ROLE_ADMIN);
    private final Cliente clienteAna = new Cliente("Ana", "ana@email.com", ana);

    @Test
    void criaPedidoParaOClienteDoUsuarioLogadoEBaixaEstoque() {
        Produto produto = new Produto("Teclado", 10, new BigDecimal("199.90"));
        when(clienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(clienteAna));
        when(produtoRepository.findById(7L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO resposta = pedidoService.criar(
                new PedidoRequestDTO(List.of(new ItemPedidoRequestDTO(7L, 3))), ana);

        assertThat(resposta.nomeCliente()).isEqualTo("Ana");
        assertThat(resposta.valorTotal()).isEqualByComparingTo("599.70");
        assertThat(resposta.itens()).singleElement().satisfies(item -> {
            assertThat(item.nomeProduto()).isEqualTo("Teclado");
            assertThat(item.subTotal()).isEqualByComparingTo("599.70");
        });
        assertThat(produto.getQuantidade()).isEqualTo(7);
    }

    @Test
    void usuarioSemCadastroDeClienteNaoFazPedido() {
        when(clienteRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(
                new PedidoRequestDTO(List.of(new ItemPedidoRequestDTO(7L, 1))), admin))
                .isInstanceOf(RegraNegocioException.class);

        verifyNoInteractions(produtoRepository, pedidoRepository);
    }

    @Test
    void estoqueInsuficienteNaoSalvaPedido() {
        Produto produto = new Produto("Teclado", 2, new BigDecimal("199.90"));
        when(clienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(clienteAna));
        when(produtoRepository.findById(7L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> pedidoService.criar(
                new PedidoRequestDTO(List.of(new ItemPedidoRequestDTO(7L, 5))), ana))
                .isInstanceOf(RegraNegocioException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void produtoInexistenteLanca404() {
        when(clienteRepository.findByUsuarioId(1L)).thenReturn(Optional.of(clienteAna));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(
                new PedidoRequestDTO(List.of(new ItemPedidoRequestDTO(99L, 1))), ana))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Produto");
    }

    @Test
    void clienteBuscaPedidoSempreFiltrandoPeloDono() {
        when(pedidoRepository.findByIdAndClienteUsuarioId(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPorId(5L, ana))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        // A consulta sem filtro de dono nunca é usada para um cliente
        verify(pedidoRepository, never()).findById(any());
    }

    @Test
    void clienteNaoPodeMarcarPedidoComoPago() {
        Pedido pedido = new Pedido(clienteAna);
        when(pedidoRepository.findByIdAndClienteUsuarioId(5L, 1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(5L, StatusPedido.PAGO, ana))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.PENDENTE);
    }

    @Test
    void adminPodeMarcarQualquerPedidoComoPago() {
        Pedido pedido = new Pedido(clienteAna);
        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));

        PedidoResponseDTO resposta = pedidoService.atualizarStatus(5L, StatusPedido.PAGO, admin);

        assertThat(resposta.statusPedido()).isEqualTo(StatusPedido.PAGO);
    }

    private static User usuario(Long id, String role) {
        User u = new User();
        u.setId(id);
        u.setEmail("u" + id + "@teste.com");
        u.setRole(role);
        return u;
    }
}
