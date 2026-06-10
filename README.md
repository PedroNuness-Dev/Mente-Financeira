# 💰🧠 Mente Financeira — API REST para Gerenciamento de Despesas Pessoais 

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?style=for-the-badge&logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![H2](https://img.shields.io/badge/H2-Database-blue?style=for-the-badge)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-85EA2D?style=for-the-badge&logo=swagger)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

---

## 📋 Sobre o Projeto 
O Mente Financeira é uma API REST desenvolvida com o objetivo de oferecer uma solução completa para o gerenciamento financeiro pessoal de usuários, permitindo o controle de despesas, categorias, pagamentos e autenticação segura. O projeto foi construído com foco em boas práticas de arquitetura, segurança, organização de código e padronização de respostas HTTP, simulando um cenário real de aplicação backend utilizada em produtos de mercado.
A aplicação permite que usuários se cadastrem, se autentiquem via JWT, gerenciem suas despesas de forma estruturada e acompanhem pagamentos, promovendo uma visão clara e organizada da vida financeira. O sistema foi projetado para ser facilmente escalável e adaptável a ambientes de desenvolvimento e produção por meio de perfis de ambiente (profiles) no Spring Boot.

### ✨ Principais Características

- ✅ **CRUD Completo** - Criar, listar, atualizar e deletar recursos no sistema
- ✅ **JWT** - Autenticação e autorização de usuários baseado em JWT pelo Spring Security
- ✅ **Validações Robustas** - Bean Validation para garantir integridade dos dados
- ✅ **Tratamento de Exceções** - Respostas HTTP padronizadas e mensagens de erro claras
- ✅ **Documentação Interativa** - Swagger UI para testar endpoints facilmente
- ✅ **Perfis de Ambiente** - Configurações separadas para desenvolvimento e produção
- ✅ **Persistência em H2 Database** - Banco de dados para testes
---

## 🚀 Tecnologias Utilizadas

### Back-end
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5** - Framework para desenvolvimento de aplicações
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM (Object-Relational Mapping)
- **Spring Security** - Autenticação e autorização

### Banco de Dados
- **H2 Database** - Banco de dados de teste relacional

### Documentação
- **SpringDoc OpenAPI 3** - Geração automática de documentação
- **Swagger UI** - Interface interativa para testes

### Validação
- **Bean Validation (Jakarta)** - Validação de dados de entrada

### Build & Deploy
- **Maven** - Gerenciamento de dependências
---

## 🛠️ Pré-requisitos

Antes de começar, você vai precisar ter instalado em sua máquina:

- [Java JDK 21+](https://www.oracle.com/java/technologies/downloads/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)


## 🔧 Como Executar o Projeto

### 1️⃣ Clone o repositório

```bash
git clone https://github.com/PedroNunes-Dev67/Menter-Financeira.git
cd Menter-Financeira
```

### 2️⃣ Execute a aplicação

```bash
# Usando Maven
mvn spring-boot:run

# Ou compilando o JAR
mvn clean package
java -jar target/Mente-Financeira-0.0.1-SNAPSHOT.jar
```

### 3️⃣ Acesse a documentação Swagger

Abra seu navegador e acesse:

```
http://localhost:8080/swagger-ui.html
```
## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

---

## 👤 Autor

**Pedro Nunes**

- 🔗 GitHub: [@PedroNunes-Dev67](https://github.com/PedroNunes-Dev67)
- 💼 LinkedIn: [Pedro Nunes](https://www.linkedin.com/in/pedro-nunes-dev67)
- 📧 Email: pedrovitornunes89@gmail.com
- 📱 Telefone: (81) 99102-4299

---

## 🙏 Agradecimentos

- [Spring Framework](https://spring.io/) - Pela excelente documentação
- [SpringDoc OpenAPI](https://springdoc.org/) - Pela facilidade de integração do Swagger

---

<div align="center">

### ⭐ Se este projeto foi útil para você, considere dar uma estrela!

**Desenvolvido com ☕ e ❤️ por Pedro Nunes**

</div>
