package com.marketcore.repository;

import com.marketcore.entity.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * O Spring Data gera o SQL a partir do nome do método:
     * WHERE LOWER(nome) LIKE LOWER('%' + nome + '%'), com paginação.
     */
    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
