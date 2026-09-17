package com.marketcore.controller;

import com.marketcore.repository.ClienteRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClienteController {

    private final ClienteRepository clienteRepository;
    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
}

