package com.marketcore;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sobe a aplicação inteira (com H2 em memória) e exercita a API pelo HTTP,
 * incluindo autenticação JWT, autorização por papel, validação e regras de negócio.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired MockMvc mvc;

    private String tokenAdmin;
    private String tokenUsuario;

    @BeforeEach
    void autenticar() throws Exception {
        tokenAdmin = login("admin@teste.com", "senhaAdmin123"); // criado pelo AdminInicializador
        tokenUsuario = registrar(emailUnico(), "senhaForte123");
    }

    // ---------- Autenticação ----------

    @Test
    void contextoSobeComTodosOsBeans() {
        // Se a configuração estiver quebrada, o @BeforeEach já falha.
    }

    @Test
    void documentacaoOpenApiEPublicaEDeclaraAutenticacaoJwt() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/pedidos']").exists());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401NoFormatoPadrao() throws Exception {
        mvc.perform(get("/produtos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.caminho").value("/produtos"));
    }

    @Test
    void tokenInvalidoRetorna401EmVezDe500() throws Exception {
        mvc.perform(get("/produtos").header("Authorization", "Bearer token.invalido.xyz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void senhaErradaRetorna401() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@teste.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));
    }

    @Test
    void cadastroIgnoraRoleEnviadaPeloCliente() throws Exception {
        String email = emailUnico();
        String token = JsonPath.read(mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"senhaForte123\",\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.token");

        // Mesmo pedindo ROLE_ADMIN, o usuário não consegue criar produtos
        mvc.perform(autenticado(post("/produtos"), token).content(produtoJson("Hack", 1, "1.00")))
                .andExpect(status().isForbidden());
    }

    @Test
    void emailDuplicadoNoCadastroRetorna409() throws Exception {
        String email = emailUnico();
        registrar(email, "senhaForte123");

        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"senhaForte123\"}"))
                .andExpect(status().isConflict());
    }

    // ---------- Produtos e clientes ----------

    @Test
    void usuarioComumNaoPodeCriarProduto() throws Exception {
        mvc.perform(autenticado(post("/produtos"), tokenUsuario).content(produtoJson("Mouse", 5, "50.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void produtoInvalidoRetorna400ComDetalhesPorCampo() throws Exception {
        mvc.perform(autenticado(post("/produtos"), tokenAdmin)
                        .content("{\"nome\":\"\",\"quantidade\":-1,\"preco\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(3)));
    }

    @Test
    void crudDeProdutoComStatusHttpCorretos() throws Exception {
        long id = criarProduto("Monitor", 3, "899.99");

        mvc.perform(autenticado(get("/produtos/" + id), tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(899.99));

        mvc.perform(autenticado(put("/produtos/" + id), tokenAdmin).content(produtoJson("Monitor 27", 8, "999.99")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(8));

        mvc.perform(autenticado(delete("/produtos/" + id), tokenAdmin))
                .andExpect(status().isNoContent());

        mvc.perform(autenticado(get("/produtos/" + id), tokenUsuario))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado com o ID: " + id));
    }

    @Test
    void listagemDeProdutosEPaginada() throws Exception {
        criarProduto("Cabo", 10, "9.90");

        mvc.perform(autenticado(get("/produtos?size=1"), tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.page.size").value(1));
    }

    @Test
    void clienteComEmailDuplicadoRetorna409() throws Exception {
        String email = emailUnico();
        criarCliente("Bia", email);

        mvc.perform(autenticado(post("/clientes"), tokenUsuario)
                        .content("{\"nome\":\"Outra\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isConflict());
    }

    // ---------- Pedidos ----------

    @Test
    void criarPedidoRetornaItensPreenchidosEBaixaEstoque() throws Exception {
        long clienteId = criarCliente("Carlos", emailUnico());
        long produtoId = criarProduto("Headset", 10, "250.00");

        mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(pedidoJson(clienteId, produtoId, 3)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/pedidos/")))
                .andExpect(jsonPath("$.statusPedido").value("PENDENTE"))
                .andExpect(jsonPath("$.valorTotal").value(750.00))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value("Headset"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(3))
                .andExpect(jsonPath("$.itens[0].subTotal").value(750.00));

        mvc.perform(autenticado(get("/produtos/" + produtoId), tokenUsuario))
                .andExpect(jsonPath("$.quantidade").value(7));
    }

    @Test
    void pedidoAlemDoEstoqueRetorna422ENaoAlteraEstoque() throws Exception {
        long clienteId = criarCliente("Duda", emailUnico());
        long produtoId = criarProduto("Webcam", 2, "150.00");

        mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(pedidoJson(clienteId, produtoId, 5)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem", containsString("Estoque insuficiente")));

        mvc.perform(autenticado(get("/produtos/" + produtoId), tokenUsuario))
                .andExpect(jsonPath("$.quantidade").value(2));
    }

    @Test
    void pedidoSemItensRetorna400() throws Exception {
        long clienteId = criarCliente("Eva", emailUnico());

        mvc.perform(autenticado(post("/pedidos"), tokenUsuario)
                        .content("{\"clienteId\":" + clienteId + ",\"itens\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelarPedidoDevolveEstoqueETransicaoInvalidaRetorna422() throws Exception {
        long clienteId = criarCliente("Fabio", emailUnico());
        long produtoId = criarProduto("SSD", 5, "399.00");
        long pedidoId = criarPedido(clienteId, produtoId, 2);

        // PENDENTE -> ENTREGUE não é permitido
        mvc.perform(autenticado(patch("/pedidos/" + pedidoId + "/status"), tokenUsuario).content("{\"status\":\"ENTREGUE\"}"))
                .andExpect(status().isUnprocessableContent());

        mvc.perform(autenticado(patch("/pedidos/" + pedidoId + "/status"), tokenUsuario).content("{\"status\":\"CANCELADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPedido").value("CANCELADO"));

        mvc.perform(autenticado(get("/produtos/" + produtoId), tokenUsuario))
                .andExpect(jsonPath("$.quantidade").value(5));
    }

    @Test
    void statusInexistenteRetorna400() throws Exception {
        mvc.perform(autenticado(get("/pedidos?status=VOANDO"), tokenUsuario))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarPedidosPorStatus() throws Exception {
        long clienteId = criarCliente("Gil", emailUnico());
        long produtoId = criarProduto("Pendrive", 10, "39.90");
        criarPedido(clienteId, produtoId, 1);

        mvc.perform(autenticado(get("/pedidos?status=PENDENTE"), tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].statusPedido", everyItem(is("PENDENTE"))))
                .andExpect(jsonPath("$.content[0].itens[0].nomeProduto").exists());
    }

    @Test
    void excluirProdutoQueEstaEmPedidoRetorna409() throws Exception {
        long clienteId = criarCliente("Hugo", emailUnico());
        long produtoId = criarProduto("Hub USB", 10, "79.90");
        criarPedido(clienteId, produtoId, 1);

        mvc.perform(autenticado(delete("/produtos/" + produtoId), tokenAdmin))
                .andExpect(status().isConflict());
    }

    // ---------- Helpers ----------

    private MockHttpServletRequestBuilder autenticado(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON);
    }

    private String login(String email, String senha) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String registrar(String email, String senha) throws Exception {
        String body = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + senha + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private long criarProduto(String nome, int qtd, String preco) throws Exception {
        String body = mvc.perform(autenticado(post("/produtos"), tokenAdmin).content(produtoJson(nome, qtd, preco)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long criarCliente(String nome, String email) throws Exception {
        String body = mvc.perform(autenticado(post("/clientes"), tokenUsuario)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long criarPedido(long clienteId, long produtoId, int qtd) throws Exception {
        String body = mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(pedidoJson(clienteId, produtoId, qtd)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String produtoJson(String nome, int qtd, String preco) {
        return "{\"nome\":\"" + nome + "\",\"quantidade\":" + qtd + ",\"preco\":" + preco + "}";
    }

    private static String pedidoJson(long clienteId, long produtoId, int qtd) {
        return "{\"clienteId\":" + clienteId + ",\"itens\":[{\"produtoId\":" + produtoId + ",\"quantidade\":" + qtd + "}]}";
    }

    private static String emailUnico() {
        return "user-" + UUID.randomUUID() + "@teste.com";
    }
}
