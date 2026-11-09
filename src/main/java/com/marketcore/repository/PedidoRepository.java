package com.marketcore.repository;

import com.marketcore.entity.Pedido;
import com.marketcore.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findByStatusPedido(StatusPedido status, Pageable pageable);
}
