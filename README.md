# CashFlow

CashFlow é uma aplicação web para **controle de caixa** por empresa e competência: registra entradas e saídas, associa movimentações a **descrições contábeis padronizadas** e gera **códigos contábeis** conforme regras do sistema.

O foco operacional é **integrar com o Questor**: o CashFlow **importa** arquivos gerados a partir do Questor (ou fluxos alinhados a ele) para criar lançamentos no caixa e **exporta** lançamentos em **TXT** prontos para **importação de volta no Questor**, reduzindo retrabalho na conferência e nos lançamentos.

---

## Integração com o Questor

| Direção | O que faz |
|--------|-----------|
| **Entrada (importação)** | Importação de **folha de pagamento** (texto exportado do Questor), **entradas** com base em relatório de conferência de saídas, além de outras rotas de importação (planilhas, PDFs, etc.) na área **Importação Geral**. |
| **Saída (exportação)** | **Exportar TXT** — gera arquivo no formato esperado para importação no Questor, com estratégias de linha por tipo de descrição (DAS, DARF, parcelamentos, folha, entre outras). |

Fluxo típico: movimentações e relatórios saem do Questor → o CashFlow consolida e classifica no caixa → ajustes e conferência → exportação TXT → retorno ao Questor.

---

## Principais funcionalidades

- Cadastro de **empresas**, **competências** e **usuários**; lançamentos vinculados a empresa, competência e descrição.
- **Lançamentos** de entrada e saída, com rastreio por descrição contábil e data de ocorrência.
- **Relatórios** de saldo por empresa e competência.
- **Exportação** de lançamentos para TXT (Questor) e fluxos de **exportação geral** na interface.
- **Importação geral** centralizada em `/importartf/todas` (folha Questor, entradas Questor, DAE, integrações, etc.).
- **TaxConnect**: consulta de pagamentos via API do IntegraContador (serviço configurável em `application.properties`).
- **DAE** (planilha com pré-visualização), **DAS** (PDF), **DARF** e importação **Excel**, conforme telas do sistema.
- Interface web com **Thymeleaf** e **Tailwind CSS** (inclui suporte a tema claro/escuro na página inicial).

---

## Tecnologias

- Java 17  
- Spring Boot 3.4 (Web, Data JPA)  
- Thymeleaf  
- PostgreSQL  
- Apache POI (Excel)  
- Apache PDFBox (PDF)  
- Lombok  

---

## Estrutura do código (pacotes principais)

- `controller` — controllers MVC (telas) e API REST onde existir  
- `service` — regras de negócio e serviços de domínio  
- `repository` — acesso a dados (Spring Data JPA)  
- `entity` — entidades e enums  
- `importacao` — importações (folha, entradas, DAE, etc.)  
- `exportacao` — exportação TXT e estratégias por descrição  
- `taxconnect` — integração TaxConnect / IntegraContador  
- `templates` — páginas HTML Thymeleaf  
- `resources` — `application.properties`, assets estáticos  

---

## Como rodar localmente

1. **Pré-requisitos:** JDK 17, Maven (ou use o wrapper `mvnw` / `mvnw.cmd` na raiz do projeto), PostgreSQL em execução.

2. **Banco de dados:** crie o banco (o exemplo abaixo usa `caixadb`) e ajuste usuário e senha em `src/main/resources/application.properties`:
   - `spring.datasource.url`
   - `spring.datasource.username`
   - `spring.datasource.password`

3. **Executar:**

   ```bash
   ./mvnw spring-boot:run
   ```

   No Windows (PowerShell):

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. **Acesso:** a porta padrão no `application.properties` do projeto é **8082** (altere `server.port` se necessário). Exemplos:
   - Início: `http://localhost:8082/`
   - Empresas: `http://localhost:8082/empresastf/todas`
   - Importação geral: `http://localhost:8082/importartf/todas`
   - Exportação: `http://localhost:8082/exportacaotf`

---

## Licença

Software proprietário. Todos os direitos reservados. Uso, cópia ou modificação apenas com autorização expressa do titular.
