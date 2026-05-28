# Trabalho Semestral QTS - Back-end Derivado

## Sobre a atividade

Este repositório foi preparado com base na atividade da disciplina, cujo enunciado determina:

- derivar um repositório isolando o projeto back-end contido na pasta `CaramelStray`;
- manter a estrutura base funcional do projeto original para as implementacoes complementares;
- evoluir o projeto com testes, analise estatica, cobertura e pipeline de CI.

Assim, este repositório passa a considerar apenas o escopo do back-end da aplicacao.

## O que foi feito neste repositório

Considerando a descricao da atividade, a organizacao adotada para este repositório e a seguinte:

- isolamento do projeto back-end originalmente contido na pasta `CaramelStray`;
- remocao dos artefatos e diretorios que nao fazem parte do escopo do back-end;
- atualizacao da documentacao para refletir o novo objetivo do repositório;
- preservacao da stack e da estrutura base do projeto Spring Boot fornecido no projeto original.

## Objetivo desta entrega

O foco deste repositório e servir como base para a continuidade da atividade avaliativa, incluindo:

- implementacao de testes de unidade;
- implementacao de testes de sistema (E2E) para as REST APIs;
- medicao de cobertura de codigo;
- integracao com SonarCloud;
- criacao de pipeline de CI com os stages `unit-test`, `static-analysis` e `system-test`.

## Stack do projeto

O back-end foi mantido na stack definida pela base original do projeto:

- Java 17+
- Spring Boot
- Maven
- PostgreSQL
- Spring Security
- JPA / Hibernate
- JWT

## Estrutura atual

O codigo-fonte do back-end esta na pasta:

```text
CaramelStray/
```

Arquivos importantes:

- `CaramelStray/pom.xml`
- `CaramelStray/src/main/java`
- `CaramelStray/src/main/resources/application.properties`
- `CaramelStray/src/test/java`

## Como executar o projeto

### Pre-requisitos

Antes de executar, tenha instalado:

- Java 17 ou superior
- PostgreSQL

O projeto inclui Maven Wrapper, entao nao e obrigatorio ter o Maven instalado globalmente.

### Configuracao do banco de dados

Atualmente o arquivo `CaramelStray/src/main/resources/application.properties` esta configurado com:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/alltallent
spring.datasource.username=postgres
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=update
```

Antes de subir a aplicacao:

1. crie um banco PostgreSQL chamado `alltallent`;
2. confirme usuario e senha do PostgreSQL;
3. ajuste o arquivo `application.properties` caso sua configuracao local seja diferente.

### Executando com Maven Wrapper

No Windows:

```powershell
cd CaramelStray
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
cd CaramelStray
./mvnw spring-boot:run
```

### Executando os testes

No Windows:

```powershell
cd CaramelStray
.\mvnw.cmd test
```

No Linux/macOS:

```bash
cd CaramelStray
./mvnw test
```

## API

A aplicacao expoe endpoints REST relacionados a autenticacao, funcionarios, perfis, competencias, areas, perguntas, avaliacoes e dashboard.

Alguns prefixos identificados no projeto:

- `/api/auth`
- `/api/funcionario`
- `/api/perfil`
- `/api/competencia`
- `/api/area`
- `/api/perguntas`
- `/api/avaliacoes`
- `/api/dashboard`

## Observacoes

- a estrutura base do back-end foi mantida, conforme exigido no enunciado;
- este repositório esta direcionado apenas ao desenvolvimento e evolucao do back-end;
- as proximas etapas da atividade incluem testes automatizados, cobertura, SonarCloud e pipeline de integracao continua.
