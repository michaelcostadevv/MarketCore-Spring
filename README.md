# 🛒 MarketCore API

Uma API RESTful de nível profissional para gestão de e-commerce, construída com Java e Spring Boot. Este projeto simula um ambiente real de vendas, incluindo gerenciamento de estoque, cálculo de pedidos e arquitetura conteinerizada.

## 🚀 Tecnologias Utilizadas
* **Back-end:** Java 21, Spring Boot, Spring Data JPA
* **Banco de Dados:** MySQL
* **Infraestrutura:** Docker e Docker Compose
* **Build e Gerenciamento:** Maven
* **Testes de API:** Postman

## ⚙️ Principais Funcionalidades
* Cadastro de Clientes e Produtos.
* Criação de Pedidos com relacionamento `@OneToMany` e `@ManyToOne`.
* **Regra de Negócio Avançada:** Baixa automática de estoque no momento da compra e validação de quantidade disponível.
* Padrão **DTO (Data Transfer Object)** para proteção e encapsulamento dos dados de entrada e saída.
* Gestão de **Status do Pedido** utilizando `Enum` (PENDENTE, PAGO, ENVIADO, ENTREGUE, CANCELADO) através de requisições `PATCH`.

## 🛠️ Como executar o projeto

1. Faça o clone deste repositório.
2. Certifique-se de ter o Docker instalado e rodando em sua máquina.
3. Gere o pacote da aplicação utilizando o Maven:
   ```bash
   .\mvnw clean package -DskipTests
