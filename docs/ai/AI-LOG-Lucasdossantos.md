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

⚠️ desconsidere esses registros(04,05e06) pois as astividades foram removidas  e refeita para alinha ao escopo do Trabalho - tirei duvidas com a professora Vania de qualidade e teste de software para sistemas de informacao. 
##registro 04
- **Data:** 01/11/2026
- **Ferramenta:** Gemini 3.6flash.
- **Objetivo:** melhoria dos teste unitario da classe EmpresaServiceTest.java
- **Prompt:** assuma que voce e um professor de teste de software  - mas faca de forma simples  novos teste unitario para a classe EmpresaServiceTest.java
- **Resultado:**  14/14 testes aprovados (regra de negócio da EmpresaService, mocks com Mockito e tratamento de exceções de aresta).
- **Uso no projeto:** public void deveRetornarErroQuandoRegimeNaoEncontradoNoCadastro() - public void deveRetornarErroQuandoCidadeNaoEncontradaNoCadastro()
- **Modificações:**  Para alinhar os testes exatamente ao comportamento real da EmpresaService,  tratar excessoes try/catch da classe EmpresaService.

##registro 05
- **Data:** 04/11/2026
- **Ferramenta:** gpt-5.6 luna(github account student)
- **Objetivo:**  elaborar teste de integracao para o sistema web pdv
- **Prompt:**  Assuma que voce e um experiente professor de teste de software - implemente um codigo java para fazer teste de integracao para a classe EmpresaService
- **Resultado:**  "Empresa salva com sucesso" - salvo na tabela tabela empresa - parâmetros persistindo em endereco - dados atualizados corretamente no banco - metodo verificaEmpresaCadastrada() retorna empresa existente
- **Uso no projeto:**  @spingBootTest para inciar o contexto real do spring - @autowired pra obter EpresaService real - jdbcTemplate para consultar a preparar dados no mysql - junit4, compativel com a configuracao atual do maven - banco mysql, configurado em application.properties
- **Modificações:** o contexto real do spring foi carregado - nao foram utilizados mocks para o empresaService - os dados foram inseridos no banco de dados real.

##registro 06
- **Data:** 01/11/2026
- **Ferramenta:**gpt-5.6 luna(github account student)
- **Objetivo:**  elaborar teste de sistema para requisito funcional e nao funcional
- **Prompt:**  Assuma que voce e um experiente engenheiro de software sua missao e fazer e justificar um teste de sistema para a classe EmpresaService nao precisa alterar o projeto apenas apresente o que deve ser feito de forma textual
(nao fuja do escopo)(se nao sabe diga nao sei) Implementar testes de sistema considerando requisitos funcionais - Pelo menos um requisito não funcional (atributos de qualidade -
Preenchimento dos dados empresariais, endereço e informações fiscais. **Desempenho:** a operação de salvar deve apresentar resposta em menos de 5 segundos. 
- **Resultado:** A página deve retornar com sucesso. O formulário de empresa deve ser exibido. Devem estar disponíveis os campos de dados cadastrais, endereço e parâmetros fiscais. Cidades, regimes tributários e ambientes devem ser carregados.
- **Uso no projeto:** implementacao de codigo em java para os seguintes requisitos funcionais: RNF de Segurança (Controle de Acesso). - RF 1 (Fluxo Real de Atualização): - RF 2 (Validação de Campo Obrigatório):
- **Modificações:**   resolver erro do maven a proposta de requisito nao funcional foi alterado - e eliminei o uso de pageobjects (dificuldade na implementacao)


Registro 07
- **Data:**07/11/2026
- **Ferramenta:** ChatGPTweb(gratuito)
- **Objetivo:** Elaborar e executar um teste automatizado de sistema para o cadastro de empresa, contemplando o preenchimento dos dados empresariais, endereço e informações fiscais.
- **Prompt:** Assuma que você é um experiente engenheiro de software. Sua missão é orientar a elaboração de um teste de sistema para o cadastro de empresa no projeto Trabalho-QeT---, utilizando Java, JUnit e Selenium WebDriver. Não altere desnecessariamente o projeto. Oriente a execução passo a passo, identifique possíveis problemas e justifique os procedimentos adotados.
- **Resultado:** Foi implementado e executado um teste automatizado para o fluxo de cadastro de empresa. O teste realizou o acesso ao sistema, a autenticação, a navegação até o formulário, o preenchimento dos dados cadastrais, do endereço e dos parâmetros fiscais, além do acionamento do botão Salvar. A execução apresentou BUILD SUCCESS, com um teste executado e sem falhas ou exceções reportadas pelo JUnit. Entretanto, não foi possível confirmar a mensagem de sucesso nem a persistência dos dados no banco de dados.
- **Uso no projeto:** Implementação e execução do teste de sistema CadastroEmpresaSistemaTest, utilizando Selenium WebDriver, Firefox em modo headless, JUnit, WebDriverManager e WebDriverWait. O teste contemplou o fluxo de autenticação e preenchimento do formulário de cadastro de empresa.
-** Modificações: **Foi ajustado o formulário empresa/form.html para explicitar o método HTTP POST (method="post"), seguido da recompilação do projeto e reconstrução do contêiner Docker. Foram realizadas investigações no controlador EmpresaController, no formulário Thymeleaf e na configuração de segurança SecurityConfig. A proteção CSRF foi identificada como desabilitada. Optou-se por não modificar o controlador nem realizar novas alterações na lógica interna da aplicação, preservando o código existente e registrando as limitações encontradas no teste.


Registro 08
-**Data:**09/11/2026
-**Ferramenta:** ChatGPTweb(gratuito)
- **Objetivo:** Elaborar testes unitários para a classe NotaFiscalService, validando seus principais métodos.
-**Prompt:** Analise a classe NotaFiscalService e implemente testes unitários para os métodos lista(), busca(), totalNotaFiscalEmitidas(), cadastrar() e geraDV(), utilizando JUnit e Mockito, sem alterar as classes de produção.
-**Resultado:** Foram criados 17 testes unitários, todos aprovados, com 80% de cobertura pelo JaCoCo.
-**Uso no projeto:** Apoio na elaboração e validação dos testes unitários da classe NotaFiscalService.
-**Modificações:** Foi criada a classe NotaFiscalServiceTest, com testes para cenários de sucesso, ausência de dados, cálculo do dígito verificador e tratamento de exceções. Não foram realizadas alterações nas classes de produção.


registro 09
- **Data:** 09/11/2026
- **Ferramenta:** gemini3.6-flash
- **Objetivo:**  Realizar teste estrutural para o modulo NotaFiscalService
- **Prompt:**  Assuma que voce é um experiente professor da universidade federal fluminense -faca o mapeamento e grafo de fluxo controle - conte as arestas - aplique a tecnica estrutural -criterio toda-arestas - use o criterio de decisao a & b a || b assuma todos valores possiveis pelo menos uma vezmétricas estáticas calculadas exclusivamente a partir do código-fonte original da classe/método sob teste;  estou certo? pergunte caso tenha duvida? 
- **Resultado:** Resolução do erro de runtime via inicialização explícita (MockitoAnnotations.initMocks(this) no @Before), resultando em BUILD SUCCESS com 13 testes executados, 0 falhas, 0 erros, e geração bem-sucedida do arquivo de cobertura target/jacoco.exec.
- **Uso no projeto:**Validação da baseline do teste estrutural para o método geraDV (cobertura de decisões em curto-circuito) e para o método cadastrar da classe NotaFiscalService, preparando a suíte de testes para a etapa subsequente de Análise de Mutação (PITest).
- **Modificações:**   Remoção da anotação de classe @RunWith(MockitoJUnitRunner.class).Adição da chamada MockitoAnnotations.initMocks(this); dentro do método @Before setUp().Ajuste de importações para compatibilidade direta com a suíte de testes Surefire no Maven.

registro 10
- **Data:** 10/11/2026
- **Ferramenta:** gemini3.6-flash
- **Objetivo:** Injetar defeitos sintéticos no código compilado (operadores alterados,   retornos modificados, limites invertidos) para avaliar a capacidade da suíte de testes de detectar falhas. 
- **Prompt:**  Assuma que voce e um experiente Professor de teste de software da universidade federal - a partir do teste feito com junit5 e tecnica estrutural - todas as arestas - ajuda com o teste de mutacao. 
- **Resultado:**Análise concluída em 4 segundos com BUILD SUCCESS. Foram gerados 34 mutantes no total, obtendo uma Força de Teste (Test Strength) de 75% sobre o código exercitado (18 mutantes mortos de 24 cobertos).
- **Uso no projeto:**Comprovação da qualidade e rigor das asserções implementadas nos critérios Toda-Arestas e Curto-Circuito (geraDV e cadastrar), garantindo que a suíte não apenas executa o código, mas detecta alterações semânticas e defeitos sintéticos no software.


Registro 11
-**Data:**10/11/2026
-**Ferramenta:** ChatGPTweb(gratuito)
- **Objetivo:** Elaborar testes funcional 
-**Prompt:** Assuma que voce e um experiente professor de teste de software; entao entenda os "html" e comportamento "do AJAX" do venda.js do projeto para que voce faca um teste funcional com selenium.
-**Resultado:** o teste automazidado testou duas funcionalidades 
-**Uso no projeto:** Realizar pedido: o teste criou um pedido, selecionou o cliente, selecionou o produto Picolé, inseriu o produto e verificou sua presença na tabela e o total de R$ 6,50.RF11 – Listar e pesquisar pedidos abertos: o teste também passou, validando a localização e a pesquisa de um pedido aberto.
-**Modificações:** nao foi implementado o teste automatizado de pagar devido ao sistema nao conseguir completar a tarefa - nao possuo agente de I.A para melhorar o sistema web


