package com.marketcore.repository;

import com.marketcore.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    /** Navega pela relação: WHERE cliente.usuario_id = :usuarioId */
    Optional<Cliente> findByUsuarioId(Long usuarioId);
}
