package com.marketcore.repository;

import com.marketcore.entity.Pedido;
import com.marketcore.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * As consultas "...ClienteUsuarioId" filtram pelo dono DENTRO da query
 * (pedido -> cliente -> usuario). Assim um pedido de outra pessoa nunca é
 * carregado, em vez de ser carregado e depois conferido com um "if".
 */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Visão do ADMIN: todos os pedidos
    Page<Pedido> findByStatusPedido(StatusPedido status, Pageable pageable);

    // Visão do CLIENTE: somente os pedidos do usuário logado
    Page<Pedido> findByClienteUsuarioId(Long usuarioId, Pageable pageable);

    Page<Pedido> findByClienteUsuarioIdAndStatusPedido(Long usuarioId, StatusPedido status, Pageable pageable);

    Optional<Pedido> findByIdAndClienteUsuarioId(Long id, Long usuarioId);
}
