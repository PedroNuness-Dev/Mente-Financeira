# 💰🧠 Mente Financeira — v2.0

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=flat&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white)
![H2](https://img.shields.io/badge/H2-blue?style=flat)
![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=flat&logo=flyway&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5-orange?style=flat)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=flat&logo=swagger&logoColor=white)
![Status](https://img.shields.io/badge/status-em_refatoração-yellow?style=flat)

API REST para controle financeiro pessoal: cada usuário tem uma carteira, e cada entrada ou saída de dinheiro vira uma movimentação categorizada — dando uma visão clara de para onde o dinheiro está indo, mês a mês.

> 🚧 **Este repositório é a reconstrução do projeto (v2.0).** A v1 tinha autenticação JWT, mas o domínio de carteira/movimentações era "bagunçado" demais para o que o produto precisava. Nesta versão a API está sendo **refeita do zero, com o domínio primeiro**: carteira com saldo real, movimentações com categorias e análise percentual de gastos. O frontend antigo foi removido de propósito — o novo painel será construído em **Angular do zero**, consumindo esta API assim que o backend estabilizar. Por enquanto, o foco total é a API.

## Sumário

- [O problema](#o-problema)
- [O que mudou na v2.0](#o-que-mudou-na-v20)
- [Como funciona](#como-funciona)
- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Modelo de domínio](#modelo-de-domínio)
- [Rodando o projeto](#rodando-o-projeto)
- [Profiles e variáveis de ambiente](#profiles-e-variáveis-de-ambiente)
- [Endpoints disponíveis](#endpoints-disponíveis)
- [Estado atual e limitações conhecidas](#estado-atual-e-limitações-conhecidas)
- [Roadmap](#roadmap)
- [Licença](#licença)

## O problema

Saber "para onde foi meu dinheiro" no fim do mês costuma exigir abrir extrato, separar por olho o que foi mercado, o que foi lazer, o que foi conta fixa. O **Mente Financeira** ataca isso registrando cada movimentação já com uma categoria (alimentação, transporte, moradia, lazer, salário, investimentos, etc.), permitindo:

1. cadastrar um usuário e abrir uma carteira vinculada a ele;
2. depositar e sacar valores, cada operação virando uma movimentação registrada;
3. consultar o saldo atual da carteira a qualquer momento;
4. ver o histórico de movimentações paginado, do mais recente para o mais antigo;
5. analisar, por mês, o percentual gasto em cada categoria sobre o total retirado.

## O que mudou na v2.0

A v1 focava em autenticação (JWT via Spring Security) com um domínio de despesas mais genérico. Na v2, a prioridade foi invertida: **o domínio financeiro vem primeiro**, modelado com mais cuidado antes de reintroduzir autenticação e o painel visual. Principais mudanças de arquitetura:

- Migração de **Spring Boot 3.5 → 4** e ajuste do domínio para **pacote por feature** (`domain/usuario`, `domain/carteira`, `domain/movimentacao`, `domain/role`), em vez da separação clássica por camada técnica.
- Banco de produção trocado de perspectiva **MySQL → PostgreSQL**, com Flyway já configurado para o dialeto Postgres.
- Introdução do conceito de **Carteira** como agregado com saldo real (antes as movimentações não impactavam um saldo persistido).
- Categorização de movimentações com **enum fechado** (`CategoriaMovimentacao`) e endpoint dedicado de **análise percentual por categoria e mês**.
- Estrutura de **Role** (`ROLE_ADMIN`, `ROLE_USUARIO`, `ROLE_MODERADOR`, `ROLE_PLUS`) já modelada no banco, preparando o terreno para a volta da autenticação/autorização.
- **MapStruct** adicionado às dependências, para futura extração da conversão entidade↔DTO que hoje ainda é feita manualmente dentro dos services.
- Frontend removido do monorepo — a v2 do painel será um projeto Angular novo, à parte.

## Como funciona

```
                ┌──────────────────────────┐
                │        Usuario           │
                │  (nome, email, roles)    │
                └────────────┬─────────────┘
                             │ 1:1
                             ▼
                ┌──────────────────────────┐
                │        Carteira          │
                │  (saldo, depositar/saquar)│
                └────────────┬─────────────┘
                             │ 1:N
                             ▼
                ┌──────────────────────────┐
                │      Movimentacao        │
                │ (valor, tipo, categoria, │
                │  data de execução)       │
                └──────────────────────────┘
```

Ao criar uma carteira já com saldo inicial, uma movimentação de `DEPOSITO` é registrada automaticamente. Todo depósito ou saque subsequente:

1. atualiza o saldo da carteira (`depositar()` / `saquar()` na própria entidade);
2. gera uma `Movimentacao` com valor, tipo (`ENTRADA`/`RETIRADA`) e categoria informada na requisição.

A análise por categoria (`/porcentagem`) soma o total retirado por categoria dentro de um mês/ano e calcula o percentual de cada uma sobre o total gasto no período.

## Stack

- **Java 21** + **Spring Boot 4**
- **Spring Data JPA** + Hibernate
- **Flyway** (dialeto PostgreSQL) — versionamento do schema
- **PostgreSQL** (produção) / **H2** (perfil de testes, modo compatibilidade PostgreSQL)
- **Bean Validation** (Jakarta) — validação de DTOs de request
- **SpringDoc OpenAPI + Swagger UI** — documentação interativa dos endpoints
- **MapStruct** — já adicionado ao projeto, para uso futuro no mapeamento DTO ↔ entidade
- **Lombok** · **Maven**

## Arquitetura

Diferente de uma separação por camada técnica (`controller/`, `service/`, `repository/` na raiz), este projeto organiza o código **por domínio de negócio**, mantendo controller, service, repository e DTOs de uma mesma feature juntos:

| Domínio | Responsável | O que faz |
|---|---|---|
| `domain/usuario` | `UsuarioController` / `UsuarioService` | cadastro de usuário, já atribuindo a role padrão (`ROLE_USUARIO`) |
| `domain/role` | `Role`, `EnumRole` | papéis de acesso do usuário (ainda sem enforcement de autorização) |
| `domain/carteira` | `CarteiraController` / `CarteiraService` | criação de carteira, depósito, saque e consulta de saldo |
| `domain/movimentacao` | `MovimentacaoController` / `MovimentacaoService` | registro interno de movimentações, histórico paginado e análise percentual por categoria |

## Estrutura do projeto

```
Mente-Financeira/
└── backend-springboot/MenteFinanceira/
    ├── src/main/java/com/pedronunesdev/MenteFinanceira/
    │   ├── domain/
    │   │   ├── usuario/       # Usuario, UsuarioController/Service/Repository, DTOs
    │   │   ├── role/          # Role, EnumRole, RoleRepository
    │   │   ├── carteira/      # Carteira, CarteiraController/Service/Repository, DTOs
    │   │   └── movimentacao/  # Movimentacao, MovimentacaoController/Service/Repository, DTOs, enums
    │   └── MenteFinanceiraApplication.java
    └── src/main/resources/
        ├── application.properties
        ├── application-test.properties
        └── db/migration/          # scripts Flyway (V1, V2)
```

## Modelo de domínio

| Entidade | Descrição |
|---|---|
| **Usuario** | nome, e-mail (único) e senha; possui uma carteira (1:1) e um conjunto de roles (N:N) |
| **Role** | papel de acesso do usuário — `ROLE_ADMIN`, `ROLE_USUARIO`, `ROLE_MODERADOR`, `ROLE_PLUS` |
| **Carteira** | saldo (`BigDecimal`) vinculado a um usuário; concentra as regras de depósito e saque |
| **Movimentacao** | valor, tipo (`ENTRADA`/`RETIRADA`), categoria e data de execução (`@CreationTimestamp`), vinculada a uma carteira |

`CategoriaMovimentacao` cobre categorias como `ALIMENTACAO`, `MERCADO`, `TRANSPORTE`, `MORADIA`, `CONTAS_FIXAS`, `SAUDE`, `EDUCACAO`, `LAZER`, `VESTUARIO`, `ASSINATURAS`, `VIAGEM`, `PET`, `PRESENTES`, `INVESTIMENTOS`, `SALARIO`, `RENDIMENTOS`, `TRANSFERENCIA`, `DEPOSITO` e `OUTROS`.

## Rodando o projeto

```bash
git clone <url-do-repositorio>
cd Mente-Financeira/backend-springboot/MenteFinanceira
./mvnw spring-boot:run
```

Por padrão a aplicação sobe com o profile `test`, usando banco H2 em memória (console habilitado).

API em `http://localhost:8080` · Swagger em `http://localhost:8080/swagger-ui.html`

## Profiles e variáveis de ambiente

| Profile | Banco | Uso |
|---|---|---|
| `test` (padrão) | H2 em memória (modo PostgreSQL) | Desenvolvimento local |
| `prod` | PostgreSQL | Produção *(perfil ainda a ser adicionado com as credenciais via variáveis de ambiente)* |

## Endpoints disponíveis

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/usuario` | cadastra um novo usuário (já com a role `ROLE_USUARIO`) |
| `POST` | `/api/carteira?idUsuario=` | cria a carteira do usuário com saldo inicial |
| `PUT` | `/api/carteira/deposito?idCarteira=` | deposita um valor na carteira e registra a movimentação |
| `PUT` | `/api/carteira/saque?idCarteira=` | saca um valor da carteira e registra a movimentação |
| `GET` | `/api/carteira/saldo?idUsuario=` | consulta o saldo atual da carteira do usuário |
| `GET` | `/api/usuario/carteira/movimentacoes?idUsuario=` | histórico paginado de movimentações |
| `GET` | `/api/usuario/carteira/movimentacoes/porcentagem?idUsuario=&mes=&ano=` | percentual gasto por categoria no mês |

Documentação completa e interativa no Swagger após subir a API.

## Estado atual e limitações conhecidas

Por ser uma refatoração em andamento, alguns pontos são propositalmente provisórios e já estão mapeados no roadmap:

- **Sem autenticação ainda** — os endpoints recebem `idUsuario`/`idCarteira` diretamente por parâmetro; a ideia é que isso venha do contexto de segurança assim que o Spring Security for reintroduzido (há comentários `TODO` no próprio código marcando esses pontos).
- **Sem handler global de exceções** — erros de negócio hoje lançam `RuntimeException`/`IllegalArgumentException` puras, sem um `ErrorResponse` padronizado.
- **Sem testes automatizados além do contexto padrão** — a suíte de testes ainda será escrita para os services de carteira, movimentação e usuário.
- **Sem Docker/Compose** — o setup de containerização será adicionado depois que a API estabilizar.

## Roadmap

- [ ] Reintroduzir autenticação e autorização (Spring Security + JWT), usando as `Role` já modeladas
- [ ] Adicionar `GlobalExceptionHandler` com respostas de erro padronizadas
- [ ] Cobertura de testes unitários (JUnit 5 + Mockito) para os services principais
- [ ] Dockerfile + Docker Compose (API + PostgreSQL)
- [ ] Novo frontend em Angular, do zero, consumindo esta API

## Licença

**Software proprietário. Todos os direitos reservados.**

Este código-fonte é de propriedade exclusiva do autor. Salvo autorização expressa e por escrito, não é permitida a cópia, redistribuição, sublicenciamento ou uso comercial deste projeto por terceiros.

---

<div align="center">

**Desenvolvido por Pedro Nunes**

</div>