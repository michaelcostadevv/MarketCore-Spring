package com.marketcore.controller;

import com.marketcore.dto.AtualizarStatusRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.enums.StatusPedido;
import com.marketcore.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO criado = pedidoService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /** Lista pedidos, opcionalmente filtrando por status: GET /pedidos?status=PAGO */
    @GetMapping
    public Page<PedidoResponseDTO> listar(
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "dataPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoService.listar(status, pageable);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @PatchMapping("/{id}/status")
    public PedidoResponseDTO atualizarStatus(@PathVariable Long id,
                                             @Valid @RequestBody AtualizarStatusRequestDTO dto) {
        return pedidoService.atualizarStatus(id, dto.status());
    }
}
