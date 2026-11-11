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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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
        mvc.perform(get("/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.caminho").value("/pedidos"));
    }

    @Test
    void tokenInvalidoRetorna401EmVezDe500() throws Exception {
        mvc.perform(get("/pedidos").header("Authorization", "Bearer token.invalido.xyz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoNaoQuebraRotaPublica() throws Exception {
        // Um token vencido guardado no navegador não pode impedir o visitante de ver a vitrine
        mvc.perform(get("/produtos").header("Authorization", "Bearer token.vencido.xyz"))
                .andExpect(status().isOk());
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
                        .content("{\"nome\":\"Hacker\",\"email\":\"" + email + "\",\"password\":\"senhaForte123\",\"role\":\"ROLE_ADMIN\"}"))
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
                        .content("{\"nome\":\"Repetido\",\"email\":\"" + email + "\",\"password\":\"senhaForte123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void cadastroCriaClienteLigadoAoUsuarioVisivelNoAuthMe() throws Exception {
        String email = emailUnico();
        String token = JsonPath.read(mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Michael\",\"email\":\"" + email + "\",\"password\":\"senhaForte123\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.token");

        mvc.perform(autenticado(get("/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nome").value("Michael"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.clienteId").isNumber());
    }

    @Test
    void authMeDoAdminIndicaAdminSemCadastroDeCliente() throws Exception {
        mvc.perform(autenticado(get("/auth/me"), tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(true))
                .andExpect(jsonPath("$.clienteId").doesNotExist());
    }

    @Test
    void authMeSemTokenRetorna401() throws Exception {
        // Prova que a regra específica "/auth/me" venceu a regra pública "/auth/**"
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroSemNomeRetorna400() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + emailUnico() + "\",\"password\":\"senhaForte123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]", containsString("nome")));
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
    void visitanteSemLoginVeAVitrineEODetalhe() throws Exception {
        long id = criarProduto("Caneca", 10, "29.90");

        mvc.perform(get("/produtos"))
                .andExpect(status().isOk());
        mvc.perform(get("/produtos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Caneca"));
    }

    @Test
    void visitanteNaoPodeAlterarOCatalogo() throws Exception {
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON).content(produtoJson("X", 1, "1.00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscaPorNomeIgnoraMaiusculasEFiltraParteDoNome() throws Exception {
        String marca = "Zyx" + UUID.randomUUID().toString().substring(0, 6); // nome único neste teste
        criarProduto("Teclado " + marca, 5, "150.00");
        criarProduto("Mouse " + marca, 5, "80.00");
        criarProduto("Monitor qualquer", 5, "900.00");

        mvc.perform(get("/produtos").param("nome", marca.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].nome", everyItem(containsString(marca))));
    }

    @Test
    void produtoComDescricaoEImagemAparecemNaResposta() throws Exception {
        String body = "{\"nome\":\"Fone\",\"quantidade\":3,\"preco\":199.90,"
                + "\"descricao\":\"Fone sem fio\",\"imagemUrl\":\"https://cdn.exemplo.com/fone.jpg\"}";

        mvc.perform(autenticado(post("/produtos"), tokenAdmin).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Fone sem fio"))
                .andExpect(jsonPath("$.imagemUrl").value("https://cdn.exemplo.com/fone.jpg"));
    }

    @Test
    void imagemComUrlPerigosaERecusada() throws Exception {
        String body = "{\"nome\":\"Fone\",\"quantidade\":3,\"preco\":199.90,\"imagemUrl\":\"javascript:alert(1)\"}";

        mvc.perform(autenticado(post("/produtos"), tokenAdmin).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]", containsString("imagemUrl")));
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

        mvc.perform(autenticado(post("/clientes"), tokenAdmin)
                        .content("{\"nome\":\"Outra\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void usuarioComumNaoAcessaCadastroDeClientes() throws Exception {
        mvc.perform(autenticado(get("/clientes"), tokenUsuario))
                .andExpect(status().isForbidden());
    }

    // ---------- Pedidos ----------

    @Test
    void criarPedidoUsaOClienteDoUsuarioLogado() throws Exception {
        long produtoId = criarProduto("Headset", 10, "250.00");

        mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(pedidoJson(produtoId, 3)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/pedidos/")))
                .andExpect(jsonPath("$.nomeCliente").value("Usuário Teste"))
                .andExpect(jsonPath("$.statusPedido").value("PENDENTE"))
                .andExpect(jsonPath("$.valorTotal").value(750.00))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value("Headset"))
                .andExpect(jsonPath("$.itens[0].subTotal").value(750.00));

        mvc.perform(get("/produtos/" + produtoId))
                .andExpect(jsonPath("$.quantidade").value(7));
    }

    @Test
    void dataDoPedidoSaiComFusoHorarioExplicitoEmUtc() throws Exception {
        long produtoId = criarProduto("Relógio", 5, "100.00");
        Instant antes = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        String body = mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(pedidoJson(produtoId, 1)))
                .andExpect(status().isCreated())
                // Com o "Z" no fim, o navegador sabe que é UTC e converte para o fuso de quem está vendo.
                // Sem fuso ("2026-11-11T10:45:00"), cada cliente adivinharia o horário de um jeito.
                .andExpect(jsonPath("$.dataPedido", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z")))
                .andReturn().getResponse().getContentAsString();

        Instant dataPedido = Instant.parse(JsonPath.read(body, "$.dataPedido"));
        assertThat(dataPedido).isBetween(antes, Instant.now().plusSeconds(1));

        // Depois de gravar e ler do banco, continua sendo o mesmo instante
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();
        String lido = mvc.perform(autenticado(get("/pedidos/" + id), tokenUsuario))
                .andReturn().getResponse().getContentAsString();
        assertThat(Instant.parse(JsonPath.read(lido, "$.dataPedido")))
                .isCloseTo(dataPedido, within(1, ChronoUnit.MILLIS));
    }

    @Test
    void clienteIdEnviadoNoCorpoEIgnorado() throws Exception {
        // Tentativa de comprar em nome de outra pessoa: o campo extra é descartado e vale o dono do token
        String tokenOutro = registrar(emailUnico(), "senhaForte123");
        long produtoId = criarProduto("Cabo HDMI", 10, "30.00");
        long idOutroCliente = clienteIdDe(tokenOutro);

        String corpo = "{\"clienteId\":" + idOutroCliente + ",\"itens\":[{\"produtoId\":" + produtoId + ",\"quantidade\":1}]}";
        mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clienteIdDe(tokenUsuario)));
    }

    @Test
    void adminSemCadastroDeClienteNaoFazPedido() throws Exception {
        long produtoId = criarProduto("Mousepad", 10, "25.00");

        mvc.perform(autenticado(post("/pedidos"), tokenAdmin).content(pedidoJson(produtoId, 1)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void pedidoAlemDoEstoqueRetorna422ENaoAlteraEstoque() throws Exception {
        long produtoId = criarProduto("Webcam", 2, "150.00");

        mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content(pedidoJson(produtoId, 5)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem", containsString("Estoque insuficiente")));

        mvc.perform(get("/produtos/" + produtoId))
                .andExpect(jsonPath("$.quantidade").value(2));
    }

    @Test
    void pedidoSemItensRetorna400() throws Exception {
        mvc.perform(autenticado(post("/pedidos"), tokenUsuario).content("{\"itens\":[]}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- Autorização por objeto (IDOR / BOLA) ----------

    @Test
    void usuarioNaoVePedidoDeOutraPessoaE404() throws Exception {
        long produtoId = criarProduto("Pendrive", 10, "39.90");
        long pedidoDaAna = criarPedido(tokenUsuario, produtoId, 1);
        String tokenBruno = registrar(emailUnico(), "senhaForte123");

        // 404 e não 403: o Bruno não pode nem descobrir que o pedido existe
        mvc.perform(autenticado(get("/pedidos/" + pedidoDaAna), tokenBruno))
                .andExpect(status().isNotFound());

        mvc.perform(autenticado(patch("/pedidos/" + pedidoDaAna + "/status"), tokenBruno)
                        .content("{\"status\":\"CANCELADO\"}"))
                .andExpect(status().isNotFound());

        // A dona continua enxergando normalmente
        mvc.perform(autenticado(get("/pedidos/" + pedidoDaAna), tokenUsuario))
                .andExpect(status().isOk());
    }

    @Test
    void listagemDoClienteMostraSoOsPropriosPedidos() throws Exception {
        long produtoId = criarProduto("Adaptador", 20, "15.00");
        criarPedido(tokenUsuario, produtoId, 1);
        String tokenBruno = registrar(emailUnico(), "senhaForte123");
        long pedidoDoBruno = criarPedido(tokenBruno, produtoId, 2);

        mvc.perform(autenticado(get("/pedidos"), tokenBruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(pedidoDoBruno));
    }

    @Test
    void adminVeTodosOsPedidos() throws Exception {
        long produtoId = criarProduto("Suporte", 20, "45.00");
        long pedidoA = criarPedido(tokenUsuario, produtoId, 1);
        long pedidoB = criarPedido(registrar(emailUnico(), "senhaForte123"), produtoId, 1);

        mvc.perform(autenticado(get("/pedidos?size=100"), tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItems((int) pedidoA, (int) pedidoB)));
    }

    @Test
    void clienteSoPodeCancelarEAdminConduzOFluxo() throws Exception {
        long produtoId = criarProduto("SSD", 5, "399.00");
        long pedidoId = criarPedido(tokenUsuario, produtoId, 2);

        // Cliente não pode marcar o próprio pedido como pago: 403
        mvc.perform(autenticado(patch("/pedidos/" + pedidoId + "/status"), tokenUsuario).content("{\"status\":\"PAGO\"}"))
                .andExpect(status().isForbidden());

        // Admin pode
        mvc.perform(autenticado(patch("/pedidos/" + pedidoId + "/status"), tokenAdmin).content("{\"status\":\"PAGO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPedido").value("PAGO"));

        // Cliente cancela o próprio pedido e o estoque volta
        mvc.perform(autenticado(patch("/pedidos/" + pedidoId + "/status"), tokenUsuario).content("{\"status\":\"CANCELADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPedido").value("CANCELADO"));

        mvc.perform(get("/produtos/" + produtoId))
                .andExpect(jsonPath("$.quantidade").value(5));
    }

    @Test
    void transicaoInvalidaRetorna422() throws Exception {
        long produtoId = criarProduto("HD", 5, "299.00");
        long pedidoId = criarPedido(tokenUsuario, produtoId, 1);

        mvc.perform(autenticado(patch("/pedidos/" + pedidoId + "/status"), tokenAdmin).content("{\"status\":\"ENTREGUE\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void statusInexistenteRetorna400() throws Exception {
        mvc.perform(autenticado(get("/pedidos?status=VOANDO"), tokenUsuario))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarPedidosPorStatus() throws Exception {
        long produtoId = criarProduto("Lanterna", 10, "39.90");
        criarPedido(tokenUsuario, produtoId, 1);

        mvc.perform(autenticado(get("/pedidos?status=PENDENTE"), tokenUsuario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].statusPedido", everyItem(is("PENDENTE"))))
                .andExpect(jsonPath("$.content[0].itens[0].nomeProduto").exists());
    }

    @Test
    void excluirProdutoQueEstaEmPedidoRetorna409() throws Exception {
        long produtoId = criarProduto("Hub USB", 10, "79.90");
        criarPedido(tokenUsuario, produtoId, 1);

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
                        .content("{\"nome\":\"Usuário Teste\",\"email\":\"" + email + "\",\"password\":\"" + senha + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private long clienteIdDe(String token) throws Exception {
        String body = mvc.perform(autenticado(get("/auth/me"), token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.clienteId")).longValue();
    }

    private long criarProduto(String nome, int qtd, String preco) throws Exception {
        String body = mvc.perform(autenticado(post("/produtos"), tokenAdmin).content(produtoJson(nome, qtd, preco)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long criarCliente(String nome, String email) throws Exception {
        String body = mvc.perform(autenticado(post("/clientes"), tokenAdmin)
                        .content("{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private long criarPedido(String token, long produtoId, int qtd) throws Exception {
        String body = mvc.perform(autenticado(post("/pedidos"), token).content(pedidoJson(produtoId, qtd)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String produtoJson(String nome, int qtd, String preco) {
        return "{\"nome\":\"" + nome + "\",\"quantidade\":" + qtd + ",\"preco\":" + preco + "}";
    }

    private static String pedidoJson(long produtoId, int qtd) {
        return "{\"itens\":[{\"produtoId\":" + produtoId + ",\"quantidade\":" + qtd + "}]}";
    }

    private static String emailUnico() {
        return "user-" + UUID.randomUUID() + "@teste.com";
    }
}
