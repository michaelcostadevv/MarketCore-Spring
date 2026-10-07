package com.marketcore.controller;

import com.marketcore.entidades.ItemPedido;
import com.marketcore.service.ItemPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itens-pedido")
public class ItemPedidoController {

    @Autowired
    private ItemPedidoService itemPedidoService;

    @GetMapping
    public ResponseEntity<List<ItemPedido>> listar() {
        return ResponseEntity.ok(itemPedidoService.listarItens());
    }

    @PostMapping
    public ResponseEntity<ItemPedido> adicionar(@RequestBody ItemPedido itemPedido) {
        ItemPedido novoItem = itemPedidoService.adicionarItem(itemPedido);
        return ResponseEntity.ok(novoItem);
    }
}