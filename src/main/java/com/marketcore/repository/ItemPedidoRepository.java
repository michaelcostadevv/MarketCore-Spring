package com.marketcore.repository;


import com.marketcore.entidades.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, String> {
    // Como o ID do item_pedido também é String, definimos <ItemPedido, String>
}
