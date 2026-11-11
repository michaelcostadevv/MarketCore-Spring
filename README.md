# 🛒 MarketCore API

[![CI](https://github.com/michaelcostadevv/MarketCore-Spring/actions/workflows/ci.yml/badge.svg)](https://github.com/michaelcostadevv/MarketCore-Spring/actions/workflows/ci.yml)

Uma API RESTful para gestão de e-commerce, construída com Java e Spring Boot. O projeto simula um ambiente real de vendas, com gerenciamento de estoque, cálculo de pedidos, autenticação JWT e arquitetura conteinerizada.

## 🚀 Tecnologias Utilizadas
* **Back-end:** Java 21, Spring Boot 4, Spring Data JPA, Spring Security
* **Autenticação:** JWT (jjwt) com controle de acesso por papel (`ROLE_USER` / `ROLE_ADMIN`)
* **Banco de Dados:** MySQL 8 (H2 em memória nos testes)
* **Documentação:** OpenAPI / Swagger UI
* **Testes:** JUnit 5, Mockito, AssertJ, MockMvc
* **Infraestrutura:** Docker e Docker Compose
* **Build:** Maven

## ⚙️ Principais Funcionalidades
* Cadastro de **clientes** e **produtos**, com listagens paginadas.
* **Pedidos** com vários itens, cálculo de subtotal e total em `BigDecimal`.
* **Baixa automática de estoque** na compra, na mesma transação do pedido: se qualquer item falhar, nada é gravado.
* **Lock otimista** (`@Version`) para evitar vender além do estoque em compras simultâneas.
* **Ciclo de vida do pedido** validado: `PENDENTE → PAGO → ENVIADO → ENTREGUE`. O cancelamento só é aceito antes do envio e **devolve os itens ao estoque**.
* **Autenticação JWT** stateless. O cadastro público sempre cria `ROLE_USER`; só `ROLE_ADMIN` altera o catálogo de produtos.
* Padrão **DTO** (records) com **Bean Validation** em todas as entradas.
* **Tratamento de erros centralizado**, com um formato único de resposta.

## 🧱 Arquitetura

```
controller  →  recebe/valida DTOs e define os status HTTP
service     →  orquestra transações e casos de uso
entity      →  regras de negócio (estoque, totais, transições de status)
repository  →  acesso a dados com Spring Data JPA
security    →  filtro JWT, regras de acesso e admin inicial
exception   →  exceções de domínio e GlobalExceptionHandler
```

## 🛠️ Como executar

### Com Docker (recomendado)

1. Clone o repositório.
2. Crie o arquivo de variáveis a partir do exemplo e troque os valores:
   ```bash
   cp .env.example .env
   ```
3. Suba tudo:
   ```bash
   docker compose up --build
   ```
   A API só inicia depois que o MySQL estiver pronto (healthcheck).

> Se você já tinha rodado uma versão anterior do projeto, recrie o volume do banco uma vez com `docker compose down -v`. O usuário do MySQL e os tipos das colunas mudaram.

### Localmente (sem Docker para a API)

Defina as variáveis de ambiente do `.env.example` (com `MARKETCORE_DB_HOST=localhost`) e rode:
```bash
./mvnw spring-boot:run
```

### Testes

Os testes usam H2 em memória e **não precisam de MySQL nem de Docker**:
```bash
./mvnw test
```

## 📖 Documentação da API

Com a aplicação rodando, acesse **http://localhost:8080/swagger-ui.html**.

1. Faça login em `POST /auth/login` (ou crie uma conta em `POST /auth/register`).
2. Copie o `token` da resposta.
3. Clique em **Authorize** e cole o token.

### Endpoints

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/auth/register` | público | Cria uma conta `ROLE_USER` e retorna o token |
| POST | `/auth/login` | público | Retorna o token JWT |
| GET | `/produtos` · `/produtos/{id}` | autenticado | Lista (paginada) / busca produtos |
| POST · PUT · DELETE | `/produtos` · `/produtos/{id}` | **ADMIN** | Gerencia o catálogo |
| GET · POST | `/clientes` | autenticado | Lista (paginada) / cadastra clientes |
| GET · PUT · DELETE | `/clientes/{id}` | autenticado | Busca / atualiza / remove um cliente |
| POST | `/pedidos` | autenticado | Cria um pedido e baixa o estoque |
| GET | `/pedidos?status=PAGO` | autenticado | Lista pedidos (filtro de status opcional) |
| GET | `/pedidos/{id}` | autenticado | Busca um pedido |
| PATCH | `/pedidos/{id}/status` | autenticado | Muda o status: `{"status": "PAGO"}` |

Paginação: `?page=0&size=20&sort=id,desc` (máximo de 100 itens por página).

### Formato de erro

Todas as respostas de erro seguem o mesmo formato:
```json
{
  "timestamp": "2026-11-09T21:00:00Z",
  "status": 422,
  "erro": "Unprocessable Content",
  "mensagem": "Estoque insuficiente para o produto 'Teclado'. Disponível: 2, solicitado: 5",
  "caminho": "/pedidos",
  "detalhes": []
}
```

| Status | Quando |
|---|---|
| 400 | Dados inválidos (`detalhes` traz o erro de cada campo) |
| 401 | Sem token, token inválido/expirado ou login incorreto |
| 403 | Usuário sem permissão (ex.: `ROLE_USER` criando produto) |
| 404 | Recurso não encontrado |
| 409 | E-mail duplicado, registro em uso ou alteração concorrente |
| 422 | Regra de negócio violada (estoque, transição de status) |
