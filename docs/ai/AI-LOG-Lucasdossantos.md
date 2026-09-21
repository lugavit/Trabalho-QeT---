# AI Log

Aluno: Lucas dos Santos Cabral da Silva 


## Registro 01

- **Data:** 13/09/2026
- **Ferramenta:** Deepseek-v4.1-flash.
- **Objetivo:** teste unitário JUnit4 + mockito - da classe `EmpresaService.
- **Prompt:** "teste unitário JUnit4 + mockito - da classe `EmpresaService`, idetifique se é necessario usar mockito e justifique para que cada teste foi criado, se for necessário envio as dependências da classe"
- **Resultado:** A IA apresentou sugestões  testes unitários para três métodos da classe EmpresaService JUnit 4 — para escrever e rodar os testes Mockito 3.4.6 — para criar simulações das dependências Eclipse — IDE onde os testes foram escritos e executados "Run As → JUnit Test" — modo de execução usado dentro do Eclipse. 
cadastro(Empresa) — 2 testes  -  verificaEmpresaCadastrada() — 2 testes: merger(...) — ramo codigo != null (atualização) — 4 testes  - merger(...) — ramo codigo == null (cadastro novo) — 4 testes.

- **Uso no projeto:**
Como a EmpresaService tem métodos void (não retornam nada), os testes usam duas técnicas:

    any(), anyLong(), anyString() -> diz "aceita qualquer valor desse tipo".

    verify(...) -> confirma que um método foi chamado.

    when(...).thenReturn(...) -> configura o que o mock deve devolver.

    doThrow(...).when(...) -> configura o mock para lançar exceção.

    assertEquals(...) -> compara o resultado (quando o método retorna String ou Optional).

 
- **Modificações:** Durante a análise do código, identifiquei 2 comportamentos não tratados:

Caso 1: se regimes.busca(codRegime) devolver Optional.empty(), o código faz:

tributario.get()  // <- lança NoSuchElementException.

Caso 2: se cidades.busca(codcidade) devolver Optional.empty(), o código faz:

cidade.get()  //<- lança NoSuchElementException.

Problema: essas exceções não são capturadas pelos try/catch da Service (porque os .get() estão fora deles). Ou seja, o método quebraria com uma exceção não tratada.

Por que não testamos:

    Não é comportamento esperado — é um bug ou caso não tratado.

    Testar exigiria @Test(expected = NoSuchElementException.class) — o que documenta o bug, mas não resolve.

    O foco do trabalho é testar o caminho feliz e os caminhos de erro tratados.

## Registro 02

- **Data:** 17/09/2026
- **Ferramenta:** Deepseek-v4.1-flash.
- **Objetivo:** ter mais clareza no código e corrigir warning do compilador java.
- **Prompt:** compilou e executou o teste unitario no eclipse contudo o compilador reclama dessa notação  - verify(parametros, times(1)).save(any(EmpresaParametro.class))                                                    when(parametros.save(any(EmpresaParametro.class)) - when(empresas.save(any(Empresa.class))) - when(empresas.save(any(Empresa.class))) - deixe o código mais legivel e facil de entender por favor (refatore o código) respeite o que orientei sobre o projeto - (use esse package e imports ).

- **Resultado:** Essa é a mais limpa e evita o problema do genérico.

- **Uso no projeto:**  doThrow(new RuntimeException("erro")).when(empresas).save(any(Empresa.class));

- **Modificações:** pradonizar o uso `doThrow(...).when(...)`.  refatoração do código e eliminar a necessidade -> any(), anyLong(), anyString() .

## Registro 03

- **Data:** 20/09/2026
- **Ferramenta:** Gemini 3.8 flash.
- **Objetivo:** ajudar a construir teste manual no test link de modo eficiente.
- **Prompt:** assuma que voce e testador experiente e tem como objetivo -> Uso da ferramenta Testlink para ao menos um cenário de teste . funcionalidade de um sistema web pdv. justifique o que fazer? e pergunte o que precisa saber?
- **Resultado:**  a ia apresentou estrategia para criar teste manual na ferramenta test link, padrao usado na aula de laboratório da professora Vania (criar projeto de teste -> criar plano de teste -> criar caso de teste -> especificar caso de teste -> adicionar suite de teste ->adiiconar ao plano de teste -> criar/build release -> criar plataformas -> adicionar plataforma ao plano de teste -> executar teste -> relatorio gerar no teste link(pdf)).
- **Uso no projeto:** os teste foi feito para a funcionalidade " adicionar forma de pagamento".
- **Modificações:** refazer o caso de teste para abranger situações de falha. porque o sistema web pdv aceita erros como {data inválida: para cartão de crédito} e {campo nulo: nome do metodo de pagamento}.

