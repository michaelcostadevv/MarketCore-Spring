package com.marketcore.controller;

import com.marketcore.dto.ProdutoRequestDTO;
import com.marketcore.dto.ProdutoResponseDTO;
import com.marketcore.entidades.Produto;
import com.marketcore.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/produtos")
public class ProdutoController{

    private ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }


    @PostMapping
// O @Valid diz ao Spring: "Antes de entrar neste método, verifique se o DTO respeita as regras!"
    public Produto cadastroProduto(@Valid @RequestBody ProdutoRequestDTO dto) {

        // Convertendo o DTO (Bandeja de entrada) para a Entidade (Panela do banco)
        Produto produtoEntidade = new Produto();
        produtoEntidade.setNome(dto.getNome());
        produtoEntidade.setQuantidade(dto.getQuantidade());
        produtoEntidade.setPreco(dto.getPreco());

        return produtoService.cadastrarProduto(produtoEntidade);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {

        // 1. Busca as "panelas" usando o SERVICE (e não o Repository)
        List<Produto> produtos = produtoService.listarProdutos(); // <--- MUDANÇA AQUI!

        // 2. Transforma cada panela num prato (DTO)
        List<ProdutoResponseDTO> produtosDTO = produtos.stream()
                .map(p -> new ProdutoResponseDTO(p.getId(), p.getNome(), p.getPreco()))
                .collect(Collectors.toList());

        // 3. Serve os pratos ao cliente
        return ResponseEntity.ok(produtosDTO);
    }
    @GetMapping("/{id}")
    public Produto buscarProdutoPorId(@PathVariable Long id){
        return produtoService.buscarProdutoPorId(id);
    }

    @PutMapping("/{id}")
    public Produto atualizarProduto(@PathVariable Long id, @RequestBody Produto produtoAtualizado){
        return produtoService.atualizarProduto(id, produtoAtualizado);
    }

    @DeleteMapping("/{id}")
    public void excluirProduto(@PathVariable Long id){
        produtoService.excluirProdutoPorId(id);
    }
}
