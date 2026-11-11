package com.marketcore.service;

import com.marketcore.dto.ProdutoRequestDTO;
import com.marketcore.dto.ProdutoResponseDTO;
import com.marketcore.entity.Produto;
import com.marketcore.exception.RecursoNaoEncontradoException;
import com.marketcore.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public ProdutoResponseDTO cadastrar(ProdutoRequestDTO dto) {
        Produto produto = new Produto();
        aplicar(produto, dto);
        return ProdutoResponseDTO.de(produtoRepository.save(produto));
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        return ProdutoResponseDTO.de(buscarEntidade(id));
    }

    /** Lista o catálogo; se {@code nome} vier preenchido, filtra por parte do nome (sem diferenciar maiúsculas). */
    public Page<ProdutoResponseDTO> listar(String nome, Pageable pageable) {
        Page<Produto> produtos = (nome == null || nome.isBlank())
                ? produtoRepository.findAll(pageable)
                : produtoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        return produtos.map(ProdutoResponseDTO::de);
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = buscarEntidade(id);
        aplicar(produto, dto);
        return ProdutoResponseDTO.de(produto);
    }

    /** Copia os dados do DTO para a entidade, num único lugar para cadastro e atualização. */
    private static void aplicar(Produto produto, ProdutoRequestDTO dto) {
        produto.setNome(dto.nome().trim());
        produto.setQuantidade(dto.quantidade());
        produto.setPreco(dto.preco());
        produto.setDescricao(vazioParaNull(dto.descricao()));
        produto.setImagemUrl(vazioParaNull(dto.imagemUrl()));
    }

    private static String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    @Transactional
    public void excluir(Long id) {
        // Se o produto estiver em algum pedido, o banco recusa e o handler responde 409
        produtoRepository.delete(buscarEntidade(id));
        produtoRepository.flush();
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }
}
