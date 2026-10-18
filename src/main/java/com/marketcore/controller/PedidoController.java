package com.marketcore.controller;

import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.enums.StatusPedido;
import com.marketcore.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Endpoint para CRIAR um novo pedido
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@RequestBody PedidoRequestDTO requestDTO) {
        PedidoResponseDTO response = pedidoService.criarPedido(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // NOVO ENDPOINT: Atualizar o STATUS do pedido
    @PatchMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusPedido status) {

        PedidoResponseDTO pedidoAtualizado = pedidoService.atualizarStatus(id, status);
        return ResponseEntity.ok(pedidoAtualizado);
    }
}