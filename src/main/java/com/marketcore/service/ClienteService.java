package com.marketcore.service;

import com.marketcore.dto.ClienteRequestDTO;
import com.marketcore.dto.ClienteResponseDTO;
import com.marketcore.entity.Cliente;
import com.marketcore.exception.ConflitoException;
import com.marketcore.exception.RecursoNaoEncontradoException;
import com.marketcore.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public ClienteResponseDTO cadastrar(ClienteRequestDTO dto) {
        String email = normalizar(dto.email());
        if (clienteRepository.existsByEmail(email)) {
            throw new ConflitoException("Já existe um cliente com o e-mail " + email);
        }
        return ClienteResponseDTO.de(clienteRepository.save(new Cliente(dto.nome().trim(), email)));
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        return ClienteResponseDTO.de(buscarEntidade(id));
    }

    public Page<ClienteResponseDTO> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(ClienteResponseDTO::de);
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarEntidade(id);
        String email = normalizar(dto.email());
        if (clienteRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflitoException("Já existe outro cliente com o e-mail " + email);
        }
        cliente.setNome(dto.nome().trim());
        cliente.setEmail(email);
        return ClienteResponseDTO.de(cliente); // entidade gerenciada: é salva no commit da transação
    }

    @Transactional
    public void excluir(Long id) {
        // Se o cliente tiver pedidos, o banco recusa e o handler responde 409
        clienteRepository.delete(buscarEntidade(id));
        clienteRepository.flush();
    }

    private Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
