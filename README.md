 ---

### 📄 **README.md – CashFlow**

````markdown
# 💸 CashFlow

CashFlow é um sistema web de controle financeiro desenvolvido com Java Spring Boot, Thymeleaf e TailwindCSS. Ele permite o registro de entradas e saídas de caixa por empresa e competência, com geração automática de códigos contábeis baseados em descrições padronizadas.

## ✨ Funcionalidades

- Cadastro de empresas e competências.
- Lançamentos de entradas e saídas com vinculação a descrições contábeis.
- Códigos contábeis gerados automaticamente conforme a descrição.
- Controle por usuário: cada usuário vê apenas suas empresas, enquanto o admin vê todas.
- Relatórios de saldo por empresa e competência.
- Interface responsiva e simples com TailwindCSS.
- Estrutura desacoplável para futura migração para frontend SPA (React/Vue/etc).

## 🧱 Tecnologias

- Java 17
- Spring Boot (REST API, JPA, Spring Security)
- Thymeleaf (para renderização server-side)
- TailwindCSS (interface e design)
- PostgreSQL (armazenamento de dados)

## ⚙️ Como rodar localmente

1. Clone o repositório:
   ```bash
   git clone https://github.com/seuusuario/cashflow.git
   cd cashflow
````

2. Configure o banco de dados PostgreSQL no arquivo `application.properties`.

3. Execute a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

4. Acesse em: [http://localhost:8080](http://localhost:8080)

## 🔐 Acesso

* Rota principal em: [http://localhost:8080/empresastf/todas](http://localhost:8080/empresastf/todas)

## 📁 Organização em camadas

* `controller` – Camada REST e MVC
* `service` – Lógica de negócios
* `repository` – Acesso a dados
* `entity` – Entidades e enums
* `templates` – Páginas Thymeleaf

## 📌 Objetivo

O CashFlow foi criado para pequenas empresas e contadores que precisam de uma ferramenta simples e eficaz para registrar movimentações financeiras com rastreabilidade contábil.

## 📄 Licença

Este é um software proprietário. Todos os direitos reservados.  
O uso, cópia ou modificação não são permitidos sem autorização expressa do autor.

---



