package com.marketcore.repository;

import com.marketcore.entidades.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Como o ID do pedido é String, definimos <Pedido, String>
}
