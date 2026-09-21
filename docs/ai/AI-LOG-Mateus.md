# AI Log

Aluno: Mateus Sacramento

## Registro 01

* **Data:** 20/09/2026
* **Ferramenta:** Google Gemini
* **Objetivo:** teste unitário JUnit4 + Mockito - da classe `CaixaLancamentoService`, identificar a necessidade de Mockito e mapear cenários de regras financeiras
* **Prompt:** "Preciso fazer testes unitários com JUnit 4 e Mockito para o sistema PDV na disciplina de Qualidade e Teste. Analise a classe CaixaLancamentoService.java abaixo. Identifique se é realmente necessário usar Mockito, aponte a complexidade de regras de negócio e liste para que cada teste deve ser criado. Se houver comportamentos não tratados ou falhas no código original, aponte-os para que eu possa justificar no trabalho."
* **Resultado:** A IA detalhou as dependências da classe, justificou a obrigatoriedade de dublês de teste para isolar a camada de persistência Spring Data JPA, mapeou 8 cenários de teste cobrindo fluxos de sucesso e exceção, e identificou uma contradição lógica na verificação de caixa aberto.
* **Uso no projeto:** `@Mock` -> cria os dublês de `CaixaLancamentoRepository` e `UsuarioService`; `@InjectMocks` -> injeta as dependências na `CaixaLancamentoService`; `@RunWith(MockitoJUnitRunner.class)` -> orquestra o ciclo de vida dos mocks no JUnit 4. Utilizados `when(...)` para simular retorno de extrato e disparos de exceção, `verify(..., times(1))` para validar salvamentos autorizados e `verify(..., never())` para assegurar que saídas com saldo insuficiente não cheguem à camada de persistência.
* **Modificações:** Mapeamento do defeito na linha 33 (`!isPresent() && map(...).isPresent()`) documentado como inconsistência lógica para abertura de issue; validação estrita da conversão de valores de sangria para números negativos (`valor * -1`) e testes de fallback para descrições padrão em campos vazios.

## Registro 02

* **Data:** 20/09/2026
* **Ferramenta:** Google Gemini
* **Objetivo:** corrigir conflito de pacotes e resolver erros de compilação da suíte na IDE (VS Code)
* **Prompt:** "Tentei colocar o código no VS Code, mas o compilador apontou 11 erros na aba Problems: The declared package 'net.originmobi.pdv.service' does not match the expected package 'net.originmobi.pdv' e tipos não resolvidos (UsuarioService, CaixaLancamentoService). No repositório do grupo, todos os testes estão em src/test/java/net/originmobi/pdv/. Ajuste o pacote e os imports necessários para compilar sem erros mantendo as diretrizes do projeto."
* **Resultado:** Reestruturação da declaração de namespace para `package net.originmobi.pdv;`, inclusão das importações explícitas das entidades e serviços do pacote `net.originmobi.pdv.service.*`, e correção da nomenclatura do mock para `UsuarioService`.
* **Uso no projeto:** Suíte consolidada com 8 métodos de teste cobrindo: suprimento com sucesso, sangria com saldo suficiente convertida para valor negativo, bloqueio de sangria com saldo insuficiente, preenchimento de observações padrão ("Sangria de caixa" e "Suprimento de caixa"), preservação de observações informadas pelo operador, tratamento de erro de banco com disparo de `RuntimeException` e consulta de extrato por caixa via `lancamentosDoCaixa`.
* **Modificações:** Adequação do arquivo ao caminho físico `src/test/java/net/originmobi/pdv/` compartilhado pelos demais integrantes do grupo, zerando as pendências de compilação e permitindo a execução completa no Test Runner for Java com 100% de sucesso (8 métodos verdes).