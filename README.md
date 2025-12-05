# ☕ Casa do Amor - Back-End (API)

Este repositório contém a lógica de servidor, regras de negócio e a API RESTful do projeto **Casa do Amor**. O sistema gerencia a persistência de dados, a segurança de acesso administrativo e as integrações com serviços de pagamento para apoiar a instituição no abrigo de pacientes com câncer.

---

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MercadoPago](https://img.shields.io/badge/MercadoPago-009EE3?style=for-the-badge&logo=mercadopago&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05033?style=for-the-badge&logo=git&logoColor=white)

---

## 📋 Índice
- [Sobre o Backend](#-sobre-o-backend)
- [Arquitetura e Funcionalidades](#-arquitetura-e-funcionalidades)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração e Execução](#-configuração-e-execução)
- [Endpoints da API](#-endpoints-da-api)
- [Autores](#-autores)

---

## 📝 Sobre o Backend

O Back-end da *Casa do Amor* foi desenvolvido para ser robusto e escalável, servindo como a "espinha dorsal" para as aplicações Web e Mobile. Ele foi construído utilizando **Java 17** e **Spring Boot**, focando em uma arquitetura limpa (MVC) para facilitar a manutenção.

O sistema é responsável por:
1.  Receber e validar dados dos formulários de inscrição e doação.
2.  Processar pagamentos de forma segura através da API do **MercadoPago**.
3.  Gerenciar a autenticação e autorização de administradores.
4.  Persistir dados de forma íntegra no banco **PostgreSQL**.

---

## ✨ Arquitetura e Funcionalidades

### 🔐 Segurança e Autenticação
- Sistema de Login exclusivo para **Administradores** e **Gerentes**.
- Proteção de rotas sensíveis (como a listagem de doadores e exclusão de registros).
- Diferenciação de níveis de acesso (`ADMIN` vs `GERENTE`).

### 💳 Integração Financeira (MercadoPago)
- Geração de **QR Code (PIX)** e Links de Pagamento para doações.
- Processamento de **Webhooks** para atualização automática do status da doação (Pendente -> Aprovado).

### 👥 Gestão de Voluntariado e Usuários
- **CRUD de Administradores:** Permite que gerentes cadastrem e gerenciem a equipe interna.
- **Processamento de Inscrições:** Recebimento e armazenamento estruturado de candidatos a voluntários com suas respectivas áreas de atuação.

### 🗄️ Persistência de Dados
- Utilização de **JDBC** e **DAOs** (Data Access Objects) para manipulação direta e otimizada do banco de dados.
- Controle transacional para garantir que operações complexas (como salvar usuário e suas permissões) sejam atômicas.

---

## 💻 Tecnologias Utilizadas

- **Java JDK 17:** Linguagem base, escolhida pela tipagem forte e robustez.
- **Spring Boot 3 (Web):** Framework para criação simplificada da API REST e injeção de dependências.
- **PostgreSQL:** Banco de dados relacional para armazenamento seguro das informações.
- **Maven:** Gerenciador de dependências e build do projeto.
- **MercadoPago SDK:** Biblioteca oficial para integração com o gateway de pagamentos.
- **JDBC (Java Database Connectivity):** Para conexão e execução de comandos SQL no banco.

---

## 🔧 Pré-requisitos

Para rodar este projeto localmente, você precisará de:

- **Java JDK 17** ou superior instalado.
- **Maven** (Opcional, caso use o wrapper `mvnw` incluído).
- **PostgreSQL** instalado e rodando na porta `5432`.
- **Git** para clonar o repositório.
- **IDE** (IntelliJ IDEA, Eclipse ou VS Code com Extension Pack for Java).

---

## 🚀 Configuração e Execução

### 1. Clone o repositório
```bash
git clone [https://github.com/seu-usuario/casa-do-amor-backend.git](https://github.com/seu-usuario/casa-do-amor-backend.git)
cd casa-do-amor-backend
