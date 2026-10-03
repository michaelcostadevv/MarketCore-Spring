package com.marketcore.service;

import com.marketcore.entidades.Produto;
import com.marketcore.exception.ProdutoNaoEncontradoException;
import com.marketcore.repository.ProdutoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto cadastrarProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public Produto buscarProdutoPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(()
                        -> new ProdutoNaoEncontradoException("Produto nao encontrado"+ id)
                );
    }

    public List<Produto> listarProdutos() {
       return produtoRepository.findAll();
    }

    public void excluirProdutoPorId(Long id) {
        buscarProdutoPorId(id);
        produtoRepository.deleteById(id);
    }

    public Produto atualizarProduto(Long id , Produto produtoAtualizado){
        Produto produtoExistente = buscarProdutoPorId(id);

        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setPreco(produtoAtualizado.getPreco());

        return  produtoRepository.save(produtoExistente);
    }
}
