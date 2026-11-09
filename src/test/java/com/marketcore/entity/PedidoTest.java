package com.marketcore.entity;

import com.marketcore.enums.StatusPedido;
import com.marketcore.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras de domínio puras: não precisam de Spring nem de banco. */
class PedidoTest {

    private Produto teclado;
    private Produto mouse;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        teclado = new Produto("Teclado", 10, new BigDecimal("199.90"));
        mouse = new Produto("Mouse", 5, new BigDecimal("0.10"));
        pedido = new Pedido(new Cliente("Ana", "ana@email.com"));
    }

    @Test
    void calculaSubtotaisETotalSemErroDeArredondamento() {
        pedido.adicionarItem(new ItemPedido(teclado, 2));
        pedido.adicionarItem(new ItemPedido(mouse, 3)); // 0.10 * 3 = 0.30 exato (com double daria 0.30000000000000004)

        assertThat(pedido.getItens()).hasSize(2);
        assertThat(pedido.getItens().get(1).getSubTotal()).isEqualByComparingTo("0.30");
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("400.10");
    }

    @Test
    void baixarEstoqueAlemDoDisponivelLancaRegraNegocio() {
        assertThatThrownBy(() -> teclado.baixarEstoque(11))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(teclado.getQuantidade()).isEqualTo(10);
    }

    @Test
    void cancelarPedidoDevolveItensAoEstoque() {
        teclado.baixarEstoque(4);
        pedido.adicionarItem(new ItemPedido(teclado, 4));

        pedido.alterarStatus(StatusPedido.CANCELADO);

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(teclado.getQuantidade()).isEqualTo(10);
    }

    @Test
    void fluxoCompletoAteEntregue() {
        pedido.alterarStatus(StatusPedido.PAGO);
        pedido.alterarStatus(StatusPedido.ENVIADO);
        pedido.alterarStatus(StatusPedido.ENTREGUE);

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.ENTREGUE);
    }

    @ParameterizedTest(name = "{0} -> {1} é proibido")
    @CsvSource({
            "PENDENTE, ENVIADO",
            "PENDENTE, ENTREGUE",
            "ENVIADO, CANCELADO",
            "ENTREGUE, PENDENTE",
            "CANCELADO, PAGO",
            "PAGO, PAGO"
    })
    void transicoesInvalidasSaoRejeitadas(StatusPedido origem, StatusPedido destino) {
        assertThat(origem.podeMudarPara(destino)).isFalse();
    }

    @Test
    void naoPermiteReabrirPedidoCancelado() {
        pedido.alterarStatus(StatusPedido.CANCELADO);

        assertThatThrownBy(() -> pedido.alterarStatus(StatusPedido.PAGO))
                .isInstanceOf(RegraNegocioException.class);
    }
}
