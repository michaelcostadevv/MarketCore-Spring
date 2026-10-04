package com.marketcore.service;

import com.marketcore.entidades.Cliente;
import com.marketcore.entidades.Pedido;
import com.marketcore.repository.ClienteRepository;
import com.marketcore.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // Listar todos os pedidos
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    // Buscar pedido por ID
    public Pedido buscarPorId(String id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
    }

    // Salvar/Cadastrar um novo pedido
    public Pedido cadastrarPedido(Pedido pedido) {
        // Valida se o cliente associado ao pedido realmente existe no banco
        if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
            throw new RuntimeException("O pedido precisa estar vinculado a um cliente válido.");
        }

        Long clienteId = pedido.getCliente().getId();
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + clienteId));

        // Associa o cliente completo encontrado no banco ao pedido
        pedido.setCliente(cliente);

        return pedidoRepository.save(pedido);
    }
}
