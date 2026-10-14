package com.marketcore.controller;

import com.marketcore.dto.ClienteResponseDTO;
import com.marketcore.dto.ClienteRequestDTO;
import com.marketcore.entidades.Cliente;
import com.marketcore.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public Cliente cadastrarCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
       Cliente clienteEntidade = new Cliente();
       clienteEntidade.setId(clienteRequestDTO.getId());
       clienteEntidade.setNome(clienteRequestDTO.getNome());
       clienteEntidade.setEmail(clienteRequestDTO.getEmail());

       return clienteService.cadastrarCliente(clienteEntidade);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientes() {
        List <Cliente> listaClientes = clienteService.listarClientes();
        List<ClienteResponseDTO> listaClienteResponseDTO = listaClientes.stream()
                .map( c -> new ClienteResponseDTO(c.getId(),c.getNome(),c.getEmail()) )
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(listaClienteResponseDTO);
    }

    @GetMapping("/{id}")
    public Cliente buscarClientePorId(@PathVariable Long id) {
        return clienteService.buscarClientePorId(id);

    }

    @PutMapping("/{id}")
    public Cliente atualizarCliente(@PathVariable Long id, @RequestBody Cliente clienteAtualizado) {
        return clienteService.atualizarCliente(id, clienteAtualizado);
    }

    @DeleteMapping("/{id}")
    public void excluirCliente(@PathVariable Long id) {
        clienteService.excluirCliente(id);
    }

}