package com.marketcore.repository;

import com.marketcore.entidades.Pedido;
import com.marketcore.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Como o ID do pedido é String, definimos <Pedido, String>
    List<Pedido> findByStatusPedido(StatusPedido status);
}
