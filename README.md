# Relatório da Atividade Semestral

## Introdução

Este documento organiza a entrega do projeto conforme os requisitos descritos na atividade da disciplina. A estrutura abaixo foi dividida em 6 seções principais, espelhando exatamente os 6 objetivos solicitados no enunciado, para facilitar o preenchimento, a revisão e a apresentação final.

O projeto base adotado para a atividade deriva do back-end contido em `CaramelStray`, mantendo a stack e a estrutura original exigidas no enunciado.

---

## 1. Análise Estática no SonarCloud

### Sobre o objetivo

Processar a análise estática do projeto na ferramenta SonarCloud, registrando o resultado inicial e configurando a inferência da cobertura de linhas de código por testes de unidade, em conformidade com os critérios definidos no item 6 da atividade.

### Resultado

No resultado inicial, o projeto ainda aparece com `Quality Gate: Not computed`, indicando que a análise foi registrada, mas que os critérios consolidados do portão de qualidade ainda não haviam sido completamente avaliados na execução observada. Em relação às métricas gerais, foram identificados `154 issues abertas`, `5,4% de cobertura de código` e `0,0% de duplicação`.

Na visão por atributos de qualidade, o projeto apresentou:
- `Security Rating: D`, com `9 security issues` abertas;
- `Reliability Rating: B`, com `8 reliability issues` abertas;
- `Maintainability Rating: A`, com `144 maintainability issues` abertas;
- `Security Hotspots: 0`, com `Security Review Rating: A`.

Na visão arquitetural, a ferramenta apontou indícios de problemas estruturais que também devem ser considerados na evolução do projeto:
- `2 tangles`;
- `1 oversized component`;
- `7 split responsibilities`;
- arquitetura pretendida ainda não definida no dashboard.

Na listagem de issues, os apontamentos iniciais ficaram distribuídos em:
- `1 bug`;
- `9 vulnerabilities`;
- `144 code smells`.

Quanto à severidade, os registros exibidos no dashboard foram:
- `0 blocker`;
- `17 critical`;
- `37 major`;
- `95 minor`;
- `5 info`.

Esses resultados mostram que o projeto já possui uma base funcional, mas ainda está distante dos critérios definidos no item 6 da atividade, principalmente em cobertura de testes e quantidade de issues abertas. Assim, este diagnóstico inicial servirá como marco comparativo para medir a evolução obtida com a implementação dos testes de unidade, testes de sistema, correções de código e integração contínua.

Evidências utilizadas nesta etapa:
- Dashboard `Overview` do SonarQube Cloud;
- painel `Summary`;
- listagem de `Issues`;
- painel `Architecture`.

Pode verificar sobre o resultados em [Evidências do Objetivo 1](./assets/Objetivo%201/)

---

## 2. Estratégia Evolutiva para Atender ao Quality Gate

### Sobre o objetivo

Definir uma estratégia evolutiva para que o projeto atenda integralmente ao Quality Gate da atividade, incluindo o tratamento de apontamentos identificados pelo SonarCloud, como `bugs`, `vulnerabilities`, `security hotspots`, `code smells`, duplicidade de código e demais issues relevantes.

### Resultado

[Insira o resultado aqui]

---

## 3. Testes de Unidade

### Sobre o objetivo

Implementar testes de unidade capazes de atender aos critérios de `code line coverage` e `branch coverage` definidos no Quality Gate do item 6.

### Resultado

[Insira o resultado aqui]

---

## 4. Testes de Sistema (E2E) para as REST APIs

### Sobre o objetivo

Implementar testes de sistema (E2E) para as REST APIs do projeto, incluindo autenticação, de forma que:

- cubram os fluxos principais;
- cubram os fluxos alternativos;
- cubram os fluxos de exceção;
- sejam implementados com `REST-assured`;
- validem, no mínimo, `response code`, `JSON Schema` e os valores retornados esperados.

### Resultado

[Insira o resultado aqui]

---

## 5. Pipeline de CI

### Sobre o objetivo

Criar uma pipeline com GitHub Actions ou GitLab CI que contemple os stages obrigatórios da atividade:

- `unit-test`: execução dos testes de unidade e geração do arquivo de cobertura consumido pelo SonarCloud;
- `static-analysis`: processamento da análise estática e publicação dos resultados no SonarCloud, incluindo `line coverage` e `branch coverage`;
- `system-test`: execução dos testes automatizados de sistema (E2E) em nível de API, cobrindo os cenários implementados.

### Resultado

[Insira o resultado aqui]

---

## 6. Apresentação dos Resultados

### Sobre o objetivo

Apresentar ao professor os resultados finais do trabalho, demonstrando a evolução do projeto em relação aos critérios da atividade e ao Quality Gate esperado.

### Resultado

[Insira o resultado aqui]

---

## Quality Gate Esperado

Para referência durante o preenchimento deste relatório, os critérios definidos no enunciado são:

| Item | Critério |
| --- | --- |
| Code line coverage | > 75% |
| Code branch coverage | == 100% |
| Bugs | == 0 |
| Vulnerabilities | == 0 |
| Duplicidade | < 5% |
| Issues - Blocker | == 0 |
| Issues - Critical | == 0 |
| Issues - Major, Minor e Info | < 5 |
