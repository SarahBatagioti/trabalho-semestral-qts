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

A estratégia evolutiva foi definida a partir do diagnóstico inicial do SonarCloud, que apontou `154 issues abertas`, `5,4% de code line coverage`, `5,4% de branch coverage`, `0,0% de duplicidade`, `1 bug`, `9 vulnerabilities`, `144 code smells`, além de `17 issues critical`, `37 major`, `95 minor` e `5 info`. Na análise arquitetural, também foram identificados `2 tangles`, `1 oversized component` e `7 split responsibilities`. Considerando o `Quality Gate` exigido na atividade, a evolução do projeto foi organizada por prioridade de risco e impacto.

<details>
<summary><b>Frente 1: Eliminação de Bloqueios e Vulnerabilidades Críticas</b></summary>
<p>

A primeira frente da estratégia consiste na eliminação total dos problemas que bloqueiam diretamente o atendimento do Quality Gate. Isso inclui corrigir o `bug` identificado em `JwtService.java`, relacionado à operação de multiplicação com risco de estouro por tipo inadequado, e resolver todas as `9 vulnerabilities`, com prioridade para as de severidade `Critical`. Entre elas, destacam-se a necessidade de validar a desativação do `CSRF` em `SecurityConfig.java` e a substituição do uso direto de entidades persistentes nas controllers por objetos específicos de transporte (`DTOs`), reduzindo exposição indevida de estrutura interna e acoplamento entre camadas. Também devem ser removidas as funcionalidades de depuração ainda ativas em produção nos controllers apontados pelo Sonar.

</p>
</details>

<details>
<summary><b>Frente 2: Resolução de Issues Críticas e Redução de Débito Técnico</b></summary>
<p>

A segunda frente está centrada na remoção das `17 issues critical`, pois o enunciado exige `Issues Critical == 0`. Parte dessas ocorrências já é coberta pelo tratamento das vulnerabilidades críticas, e o restante está concentrado principalmente em problemas de manutenibilidade, como duplicação de literais e uso de curingas genéricos. Para isso, a estratégia prevê: extração de constantes para valores repetidos como papéis de acesso e mensagens de erro; substituição de tipos genéricos com wildcard por assinaturas mais explícitas; e padronização de trechos repetidos para reduzir risco de inconsistência e facilitar manutenção. Essa etapa reduz débito técnico, melhora legibilidade e aproxima o projeto da meta de manter `major`, `minor` e `info` abaixo de `5`.

</p>
</details>

<details>
<summary><b>Frente 3: Abordagem de Code Smells e Preservação de Duplicidade Zero</b></summary>
<p>

A terceira frente trata os `code smells`, priorizando os de maior concentração e impacto estrutural antes dos meramente cosméticos. Em vez de tentar eliminar os `144 code smells` indiscriminadamente, a abordagem será orientada por agrupamento: primeiro corrigir smells críticos e maiores; depois atacar problemas repetitivos de convenção, design e consistência que possam ser resolvidos em lote. Nessa etapa entram ações como renomeação e reorganização de pacotes, extração de responsabilidades excessivas, redução de acoplamento entre controllers e services, e adequação de convenções de nomenclatura e estrutura. Como a duplicidade já está em `0,0%`, a prioridade não é reduzir cópia de código, mas preservar esse resultado durante as refatorações.

</p>
</details>

<details>
<summary><b>Frente 4: Ampliação da Cobertura de Testes (Linhas e Ramos)</b></summary>
<p>

A quarta frente é a evolução da cobertura de testes, essencial para atender simultaneamente os critérios de `code line coverage > 75%` e `code branch coverage == 100%`. A estratégia é começar pelos componentes mais críticos para a qualidade e para a segurança do sistema, especialmente autenticação, configuração de segurança, services com regras de negócio e controllers com múltiplos fluxos. Os testes de unidade devem cobrir fluxos principais, alternativos e de exceção, com foco explícito em decisões condicionais, tratamento de erros e regras de autorização, para elevar a cobertura de linhas e, principalmente, garantir cobertura total de ramos. A correção de issues e a escrita de testes serão conduzidas em paralelo, de forma que cada refatoração relevante já seja protegida por testes automatizados.

</p>
</details>

<details>
<summary><b>Frente 5: Refatoração Arquitetural e Melhoria de Modularidade</b></summary>
<p>

A quinta frente aborda os problemas arquiteturais identificados pelo Sonar, mesmo que eles não apareçam diretamente no Quality Gate como critério numérico. Os `2 tangles`, `1 oversized component` e `7 split responsibilities` indicam necessidade de melhorar separação de responsabilidades e modularidade. Assim, a estratégia inclui refatorar classes com múltiplas responsabilidades, redistribuir regras de negócio excessivamente concentradas, e reforçar o papel de cada camada da aplicação. Essa melhoria estrutural reduz a chance de regressões, facilita a manutenção e sustenta a evolução dos testes e da pipeline.

</p>
</details>

<br>

Por fim, a estratégia será operacionalizada em ciclos curtos e verificáveis. A ordem de execução definida é: `1)` corrigir bug e vulnerabilities; `2)` zerar issues critical; `3)` elevar cobertura com testes de unidade; `4)` reduzir issues major, minor e info para abaixo do limite; `5)` consolidar refatorações arquiteturais; e `6)` validar continuamente os resultados via pipeline com análise estática, testes de unidade e testes de sistema. Com essa abordagem evolutiva, o projeto passa a atacar primeiro os itens mandatórios do Quality Gate e, ao mesmo tempo, melhora sua confiabilidade, segurança, testabilidade e manutenibilidade.

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
