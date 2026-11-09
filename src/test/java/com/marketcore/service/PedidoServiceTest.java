package com.marketcore.service;

import com.marketcore.dto.ItemPedidoRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entity.Cliente;
import com.marketcore.entity.Pedido;
import com.marketcore.entity.Produto;
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

    private final Cliente cliente = new Cliente("Ana", "ana@email.com");

    @Test
    void criaPedidoComItensPreenchidosEBaixaEstoque() {
        Produto produto = new Produto("Teclado", 10, new BigDecimal("199.90"));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(7L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO resposta = pedidoService.criar(
                new PedidoRequestDTO(1L, List.of(new ItemPedidoRequestDTO(7L, 3))));

        assertThat(resposta.valorTotal()).isEqualByComparingTo("599.70");
        assertThat(resposta.itens()).singleElement().satisfies(item -> {
            assertThat(item.nomeProduto()).isEqualTo("Teclado");
            assertThat(item.quantidade()).isEqualTo(3);
            assertThat(item.precoUnitario()).isEqualByComparingTo("199.90");
            assertThat(item.subTotal()).isEqualByComparingTo("599.70");
        });
        assertThat(produto.getQuantidade()).isEqualTo(7);
    }

    @Test
    void estoqueInsuficienteNaoSalvaPedido() {
        Produto produto = new Produto("Teclado", 2, new BigDecimal("199.90"));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(7L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> pedidoService.criar(
                new PedidoRequestDTO(1L, List.of(new ItemPedidoRequestDTO(7L, 5)))))
                .isInstanceOf(RegraNegocioException.class);

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void produtoInexistenteLanca404() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(
                new PedidoRequestDTO(1L, List.of(new ItemPedidoRequestDTO(99L, 1)))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Produto");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void clienteInexistenteLanca404() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(
                new PedidoRequestDTO(1L, List.of(new ItemPedidoRequestDTO(7L, 1)))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Cliente");

        verifyNoInteractions(produtoRepository, pedidoRepository);
    }
}
