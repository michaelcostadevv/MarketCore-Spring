package com.marketcore.controller;

import com.marketcore.dto.ItemPedidoResponseDTO;
import com.marketcore.dto.PedidoRequestDTO;
import com.marketcore.dto.PedidoResponseDTO;
import com.marketcore.entidades.Pedido;
import com.marketcore.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // --- POST: CRIAR PEDIDO (Usando RequestDTO na entrada) ---
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> cadastrarPedido(@Valid @RequestBody PedidoRequestDTO dto) {

        // 1. Envia o DTO para o Service criar o Pedido real no banco de dados
        Pedido pedidoSalvo = pedidoService.cadastrarPedido(dto);

        // 2. Converte o Pedido salvo para o ResponseDTO (para devolver um JSON limpo)
        PedidoResponseDTO respostaDTO = converterParaDTO(pedidoSalvo);

        // 3. Devolve Status 201 (Created) e o JSON do pedido formatado
        return ResponseEntity.status(HttpStatus.CREATED).body(respostaDTO);
    }

    // --- GET: LISTAR PEDIDOS (Usando ResponseDTO na saída) ---
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {

        List<Pedido> pedidos = pedidoService.listarTodos(); // Adapte o nome do método se necessário

        List<PedidoResponseDTO> pedidosDTO = pedidos.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(pedidosDTO);
    }

    // --- GET: BUSCAR POR ID ---
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {

        Pedido pedido = pedidoService.buscarPorId(id); // Adapte o nome do método se necessário

        return ResponseEntity.ok(converterParaDTO(pedido));
    }

    // --- MÉTODO AUXILIAR PRIVADO: CONVERSÃO DE ENTIDADE PARA DTO ---
    private PedidoResponseDTO converterParaDTO(Pedido pedido) {

        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens().stream()
                .map(item -> new ItemPedidoResponseDTO(
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getProduto().getPreco()
                ))
                .collect(Collectors.toList());

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getCliente().getNome(),
                pedido.getValorTotal(),
                itensDTO
        );
    }
}