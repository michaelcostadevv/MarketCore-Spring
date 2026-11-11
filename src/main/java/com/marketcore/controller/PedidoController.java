package com.marketcore.controller;

import com.marketcore.dto.AtualizarStatusRequestDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entity.User;
import com.marketcore.enums.StatusPedido;
import com.marketcore.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * O controller apenas repassa o usuário logado; as regras de quem vê o quê ficam no PedidoService,
 * para valerem em qualquer ponto de entrada.
 */
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO dto,
                                                   @AuthenticationPrincipal User usuario) {
        PedidoResponseDTO criado = pedidoService.criar(dto, usuario);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /** Cliente: "meus pedidos". Admin: todos. Filtro opcional: GET /pedidos?status=PAGO */
    @GetMapping
    public Page<PedidoResponseDTO> listar(
            @RequestParam(required = false) StatusPedido status,
            @AuthenticationPrincipal User usuario,
            @PageableDefault(size = 20, sort = "dataPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoService.listar(status, usuario, pageable);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscarPorId(@PathVariable Long id, @AuthenticationPrincipal User usuario) {
        return pedidoService.buscarPorId(id, usuario);
    }

    @PatchMapping("/{id}/status")
    public PedidoResponseDTO atualizarStatus(@PathVariable Long id,
                                             @Valid @RequestBody AtualizarStatusRequestDTO dto,
                                             @AuthenticationPrincipal User usuario) {
        return pedidoService.atualizarStatus(id, dto.status(), usuario);
    }
}
