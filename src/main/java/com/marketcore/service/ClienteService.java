package com.marketcore.service;

import com.marketcore.entidades.Cliente;
import com.marketcore.exception.ClienteNaoEncontradoException;
import com.marketcore.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente cadastrarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente buscarClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new ClienteNaoEncontradoException("Cliente não encontrado: " + id)
                );
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public void excluirCliente(Long id) {
        buscarClientePorId(id);
        clienteRepository.deleteById(id);
    }

    public Cliente atualizarCliente(Long id, Cliente clienteAtualizado) {
        Cliente clienteExistente = buscarClientePorId(id);

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setEmail(clienteAtualizado.getEmail());

        return clienteRepository.save(clienteExistente);
    }
}