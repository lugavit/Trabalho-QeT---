# AI Log

**Aluno:** Lucas dos Santos Cabral da Silva

---

## Registro 01

- **Data:** 13/09/2026
- **Ferramenta:** DeepSeek-V4.1-Flash
- **Objetivo:** Criar testes unitários com JUnit 4 + Mockito para a classe `EmpresaService`.
- **Prompt:** "teste unitário JUnit4 + mockito - da classe `EmpresaService`, identifique se é necessário usar mockito e justifique para que cada teste foi criado, se for necessário envio as dependências da classe"
- **Resultado:** A IA apresentou sugestões de testes unitários para três métodos da classe `EmpresaService`, utilizando JUnit 4 para escrever e rodar os testes, Mockito 3.4.6 para criar simulações das dependências e a IDE Eclipse ("Run As → JUnit Test") para execução:
  - `cadastro(Empresa)` — 2 testes;
  - `verificaEmpresaCadastrada()` — 2 testes;
  - `merger(...)` — ramo `codigo != null` (atualização) — 4 testes;
  - `merger(...)` — ramo `codigo == null` (cadastro novo) — 4 testes.
- **Uso no projeto:** Como a `EmpresaService` possui métodos `void` (sem retorno), os testes utilizaram as seguintes técnicas:
  - `any()`, `anyLong()`, `anyString()` — aceita qualquer valor daquele tipo;
  - `verify(...)` — confirma que um método foi chamado;
  - `when(...).thenReturn(...)` — configura o retorno esperado do mock;
  - `doThrow(...).when(...)` — configura o mock para lançar exceção;
  - `assertEquals(...)` — compara o resultado (quando o método retorna `String` ou `Optional`).
- **Modificações:** Durante a análise do código, foram identificados dois comportamentos não tratados:
  - **Caso 1:** Se `regimes.busca(codRegime)` devolver `Optional.empty()`, o código executa `tributario.get()`, lançando `NoSuchElementException`.
  - **Caso 2:** Se `cidades.busca(codcidade)` devolver `Optional.empty()`, o código executa `cidade.get()`, lançando `NoSuchElementException`.
  - **Problema e decisão:** Essas exceções não são capturadas pelos blocos `try/catch` da `Service` (pois os `.get()` estão fora deles), fazendo o método quebrar. Optou-se por não testá-las por não representarem comportamento esperado (testar com `@Test(expected = NoSuchElementException.class)` apenas documentaria o bug sem resolvê-lo), mantendo o foco no caminho feliz e nos erros tratados.

## Registro 02

- **Data:** 17/09/2026
- **Ferramenta:** DeepSeek-V4.1-Flash
- **Objetivo:** Obter mais clareza no código e corrigir *warnings* do compilador Java.
- **Prompt:** "compilou e executou o teste unitario no eclipse contudo o compilador reclama dessa notação - verify(parametros, times(1)).save(any(EmpresaParametro.class)) when(parametros.save(any(EmpresaParametro.class)) - when(empresas.save(any(Empresa.class))) - deixe o código mais legivel e facil de entender por favor (refatore o código) respeite o que orientei sobre o projeto - (use esse package e imports )."
- **Resultado:** A IA sugeriu uma abordagem mais limpa que evita o problema de tipos genéricos no compilador.
- **Uso no projeto:** Aplicação da estrutura `doThrow(new RuntimeException("erro")).when(empresas).save(any(Empresa.class));`.
- **Modificações:** Padronização do uso de `doThrow(...).when(...)` e refatoração do código para eliminar o uso excessivo de `any()`, `anyLong()` e `anyString()`.

## Registro 03

- **Data:** 20/09/2026
- **Ferramenta:** Gemini 3.8 Flash
- **Objetivo:** Construir testes manuais no TestLink de modo eficiente.
- **Prompt:** "assuma que voce e testador experiente e tem como objetivo -> Uso da ferramenta Testlink para ao menos um cenário de teste . funcionalidade de um sistema web pdv. justifique o que fazer? e pergunte o que precisa saber?"
- **Resultado:** A IA apresentou uma estratégia para criação de testes manuais no TestLink seguindo o padrão utilizado nas aulas de laboratório da professora Vânia (criar projeto de teste → criar plano de teste → criar caso de teste → especificar caso de teste → adicionar suíte de teste → adicionar ao plano de teste → criar build/release → criar plataformas → adicionar plataforma ao plano de teste → executar teste → gerar relatório em PDF no TestLink).
- **Uso no projeto:** Os testes foram elaborados para a funcionalidade "Adicionar forma de pagamento".
- **Modificações:** Refatoração dos casos de teste para abranger situações de falha, pois o sistema web PDV aceitava entradas inválidas, como data inválida para cartão de crédito e campo nulo no nome do método de pagamento.

---

> ⚠️ **Nota:** Desconsidere os registros **04, 05 e 06**, pois essas atividades foram removidas e refeitas para alinhar o projeto ao escopo do trabalho após esclarecimento de dúvidas com a professora Vânia (Qualidade e Teste de Software para Sistemas de Informação).

## Registro 04

- **Data:** 01/11/2026
- **Ferramenta:** Gemini 3.6 Flash
- **Objetivo:** Melhoria dos testes unitários da classe `EmpresaServiceTest.java`.
- **Prompt:** "assuma que voce e um professor de teste de software - mas faca de forma simples novos teste unitario para a classe EmpresaServiceTest.java"
- **Resultado:** 14/14 testes aprovados, cobrindo regras de negócio da `EmpresaService`, mocks com Mockito e tratamento de exceções de aresta.
- **Uso no projeto:** Implementação dos métodos `deveRetornarErroQuandoRegimeNaoEncontradoNoCadastro()` e `deveRetornarErroQuandoCidadeNaoEncontradaNoCadastro()`.
- **Modificações:** Alinhamento dos testes ao comportamento real da `EmpresaService`, contemplando o tratamento de exceções nos blocos `try/catch` da classe.

## Registro 05

- **Data:** 04/11/2026
- **Ferramenta:** GPT-5.6 Luna (GitHub Student)
- **Objetivo:** Elaborar testes de integração para o sistema web PDV.
- **Prompt:** "Assuma que voce e um experiente professor de teste de software - implemente um codigo java para fazer teste de integracao para a classe EmpresaService"
- **Resultado:** Validação dos cenários: "Empresa salva com sucesso", persistência na tabela `empresa`, persistência de parâmetros em `endereco`, atualização correta dos dados no banco e retorno da empresa existente pelo método `verificaEmpresaCadastrada()`.
- **Uso no projeto:** Uso de `@SpringBootTest` para iniciar o contexto real do Spring, `@Autowired` para injetar o `EmpresaService` real, `JdbcTemplate` para consultar e preparar dados no MySQL, JUnit 4 (compatível com a configuração do Maven) e banco MySQL configurado via `application.properties`.
- **Modificações:** Carregamento do contexto real do Spring sem uso de mocks para a `EmpresaService`, com inserção real dos dados no banco de dados.

## Registro 06

- **Data:** 01/11/2026
- **Ferramenta:** GPT-5.6 Luna (GitHub Student)
- **Objetivo:** Elaborar testes de sistema para requisitos funcionais e não funcionais.
- **Prompt:** "Assuma que voce e um experiente engenheiro de software sua missao e fazer e justificar um teste de sistema para a classe EmpresaService nao precisa alterar o projeto apenas apresente o que deve ser feito de forma textual (nao fuja do escopo)(se nao sabe diga nao sei) Implementar testes de sistema considerando requisitos funcionais - Pelo menos um requisito não funcional (atributos de qualidade - Preenchimento dos dados empresariais, endereço e informações fiscais. Desempenho: a operação de salvar deve apresentar resposta em menos de 5 segundos."
- **Resultado:** Definição dos critérios de aceitação: carregamento bem-sucedido da página, exibição do formulário de empresa com campos de dados cadastrais, endereço e parâmetros fiscais, além do carregamento de cidades, regimes tributários e ambientes.
- **Uso no projeto:** Implementação de código em Java para validar o RNF de Segurança (Controle de Acesso), RF 1 (Fluxo Real de Atualização) e RF 2 (Validação de Campo Obrigatório).
- **Modificações:** Resolução de erro no Maven, alteração da proposta do requisito não funcional e remoção do padrão *Page Objects* devido à dificuldade de implementação.

---

## Registro 07

- **Data:** 07/11/2026
- **Ferramenta:** ChatGPT Web (gratuito)
- **Objetivo:** Elaborar e executar um teste automatizado de sistema para o cadastro de empresa, contemplando o preenchimento dos dados empresariais, endereço e informações fiscais.
- **Prompt:** "Assuma que você é um experiente engenheiro de software. Sua missão é orientar a elaboração de um teste de sistema para o cadastro de empresa no projeto Trabalho-QeT---, utilizando Java, JUnit e Selenium WebDriver. Não altere desnecessariamente o projeto. Oriente a execução passo a passo, identifique possíveis problemas e justifique os procedimentos adotados."
- **Resultado:** Implementação e execução de teste automatizado para o fluxo de cadastro de empresa (acesso, autenticação, navegação até o formulário, preenchimento dos dados cadastrais, endereço e parâmetros fiscais, e acionamento do botão Salvar). A execução apresentou `BUILD SUCCESS` (1 teste executado, sem falhas ou exceções reportadas pelo JUnit), embora não tenha sido possível confirmar a mensagem de sucesso nem a persistência no banco de dados.
- **Uso no projeto:** Implementação e execução da classe `CadastroEmpresaSistemaTest`, utilizando Selenium WebDriver, Firefox em modo *headless*, JUnit, WebDriverManager e `WebDriverWait`.
- **Modificações:** Ajuste no formulário `empresa/form.html` para explicitar o método HTTP POST (`method="post"`), seguido da recompilação do projeto e reconstrução do contêiner Docker. Foram investigados o `EmpresaController`, o formulário Thymeleaf e o `SecurityConfig` (onde a proteção CSRF estava desabilitada). Optou-se por não alterar o controlador nem a lógica interna da aplicação, registrando as limitações encontradas.

## Registro 08

- **Data:** 09/11/2026
- **Ferramenta:** ChatGPT Web (gratuito)
- **Objetivo:** Elaborar testes unitários para a classe `NotaFiscalService`, validando seus principais métodos.
- **Prompt:** "Analise a classe NotaFiscalService e implemente testes unitários para os métodos lista(), busca(), totalNotaFiscalEmitidas(), cadastrar() e geraDV(), utilizando JUnit e Mockito, sem alterar as classes de produção."
- **Resultado:** Criação de 17 testes unitários, todos aprovados, alcançando 80% de cobertura pelo JaCoCo.
- **Uso no projeto:** Apoio na elaboração e validação dos testes unitários da classe `NotaFiscalService`.
- **Modificações:** Criação da classe `NotaFiscalServiceTest`, contendo testes para cenários de sucesso, ausência de dados, cálculo do dígito verificador e tratamento de exceções, sem alterações nas classes de produção.

## Registro 09

- **Data:** 09/11/2026
- **Ferramenta:** Gemini 3.6 Flash
- **Objetivo:** Realizar teste estrutural para o módulo `NotaFiscalService`.
- **Prompt:** "Assuma que voce é um experiente professor da universidade federal fluminense -faca o mapeamento e grafo de fluxo controle - conte as arestas - aplique a tecnica estrutural -criterio toda-arestas - use o criterio de decisao a & b a || b assuma todos valores possiveis pelo menos uma vezmétricas estáticas calculadas exclusivamente a partir do código-fonte original da classe/método sob teste; estou certo? pergunte caso tenha duvida?"
- **Resultado:** Resolução do erro de *runtime* via inicialização explícita (`MockitoAnnotations.initMocks(this)` no `@Before`), resultando em `BUILD SUCCESS` com 13 testes executados, 0 falhas, 0 erros e geração bem-sucedida do relatório de cobertura `target/jacoco.exec`.
- **Uso no projeto:** Validação da *baseline* do teste estrutural para os métodos `geraDV` (cobertura de decisões em curto-circuito) e `cadastrar` da classe `NotaFiscalService`, preparando a suíte para a etapa de Análise de Mutação (PITest).
- **Modificações:** Remoção da anotação de classe `@RunWith(MockitoJUnitRunner.class)`, adição da chamada `MockitoAnnotations.initMocks(this);` dentro do método `@Before setUp()` e ajuste de importações para compatibilidade com o plugin Surefire no Maven.

## Registro 10

- **Data:** 10/11/2026
- **Ferramenta:** Gemini 3.6 Flash
- **Objetivo:** Injetar defeitos sintéticos no código compilado (operadores alterados, retornos modificados, limites invertidos) para avaliar a capacidade da suíte de testes de detectar falhas.
- **Prompt:** "Assuma que voce e um experiente Professor de teste de software da universidade federal - a partir do teste feito com junit5 e tecnica estrutural - todas as arestas - ajuda com o teste de mutacao."
- **Resultado:** Análise concluída em 4 segundos com `BUILD SUCCESS`. Foram gerados 34 mutantes no total, obtendo uma Força de Teste (*Test Strength*) de 75% sobre o código exercitado (18 mutantes mortos de 24 cobertos).
- **Uso no projeto:** Comprovação do rigor das asserções implementadas nos critérios Toda-Arestas e Curto-Circuito (`geraDV` e `cadastrar`), garantindo a detecção de alterações semânticas e defeitos sintéticos.
- **Modificações:** Nenhuma alteração nas classes de produção; ajustes realizados apenas na suíte de testes estruturais para ampliar a detecção de mutantes.

## Registro 11

- **Data:** 10/11/2026
- **Ferramenta:** ChatGPT Web (gratuito)
- **Objetivo:** Elaborar testes funcionais automatizados.
- **Prompt:** "Assuma que voce e um experiente professor de teste de software; entao entenda os "html" e comportamento "do AJAX" do venda.js do projeto para que voce faca um teste funcional com selenium."
- **Resultado:** O teste automatizado validou duas funcionalidades do fluxo de vendas.
- **Uso no projeto:**
  - **Realizar pedido:** Criação de pedido, seleção de cliente e do produto "Picolé", inserção do item e validação da presença na tabela e do valor total de R$ 6,50.
  - **RF11 – Listar e pesquisar pedidos abertos:** Validação da localização e pesquisa de um pedido aberto.
- **Modificações:** Não foi implementado o teste automatizado para o fluxo de pagamento devido a limitações da aplicação em concluir a operação durante a automação.

## Registro 12

- **Data:** 20/11/2026
- **Ferramenta:** ChatGPT Web (gratuito)
- **Objetivo:** Realizar testes de integração das requisições HTTP da aplicação PDV, verificando a autenticação do usuário e a comunicação entre as camadas de controle, serviço e persistência de dados.
- **Prompt:** "Solicitei auxílio para configurar e executar requisições no Postman, validar a autenticação por meio de sessão, identificar erros HTTP e verificar o funcionamento das operações de cadastro e listagem de categorias."
- **Resultado:** Autenticação do usuário gerente, cadastro de categorias via requisição `POST` e consulta de registros via `GET`, obtendo respostas HTTP `302` e `200`, respectivamente, conforme o fluxo esperado.
- **Uso no projeto:** Configuração e validação dos testes de integração no Postman, identificando e corrigindo o *endpoint* incorreto da requisição `POST` (`/categoria/form` em vez de `/categoria`), além da verificação da sessão autenticada e da persistência no banco de dados.
- **Modificações:** Correção da URL da requisição `POST` para `/categoria` nos testes do Postman, conforme mapeamento do *controller*, sem alterações no código-fonte da aplicação.

## Registro 13

- **Data:** 21/11/2026
- **Ferramenta:** ChatGPT Web (gratuito)
- **Objetivo:** Avaliar o desempenho da aplicação PDV por meio de testes de carga, observando os tempos de resposta, a vazão e a ocorrência de erros durante as requisições.
- **Prompt:** "Solicitei auxílio para configurar e interpretar os testes de desempenho do Postman, comparar diferentes execuções e analisar métricas como tempo médio de resposta, P95, throughput e taxa de erros."
- **Resultado:** Comparação entre 1 e 2 usuários virtuais: a vazão (*throughput*) aumentou de 17,85 para 34,20 requisições/segundo; o tempo médio passou de 46 ms para 48 ms; o P95 passou de 96 ms para 105 ms; e não houve registro de erros ou falhas.
- **Uso no projeto:** Interpretação dos resultados dos testes de desempenho no Postman e comparação entre execuções sob diferentes cargas.
- **Modificações:** Alteração da quantidade de usuários virtuais de 1 para 2 na execução mais recente, sem alterações no código-fonte da aplicação.

## Registro 14

- **Data:** 21/11/2026
- **Ferramenta:** ChatGPT Luna 5.6
- **Objetivo:** Corrigir os problemas de qualidade e boas práticas identificados no módulo `NotaFiscalService.java`.
- **Prompt:** "Assuma que voce e um experiente desenvolvedor java 8 + maven e implemente as solucoes para esses conjuntos de items do relatorio sonar qube(plugin da IDEecilpse)"
- **Resultado:** Aplicação das correções apontadas pela análise estática no módulo.
- **Uso no projeto:** Melhoria da legibilidade, segurança, manutenibilidade e portabilidade do serviço responsável pelas operações de notas fiscais.
- **Modificações:**
  - Renomear variáveis locais para o padrão `camelCase`.
  - Substituir exceções genéricas por exceções específicas ou pela exceção personalizada `NotaFiscalException`.
  - Substituir chamadas a `System.out.println` por logger SLF4J.
  - Substituir a exclusão com `File.delete()` por `java.nio.file.Files.delete`.
  - Tratar adequadamente as exceções de acesso ao sistema de arquivos.
  - Substituir APIs antigas de data e hora pela API `java.time`.
  - Informar explicitamente o `ZoneId` nas chamadas a `now()`.
  - Remover o campo `dataAtual` e declará-lo como variável local no método.
  - Verificar `Optional.isPresent()` antes de acessar o valor com `get()`.
  - Substituir a injeção por campo por injeção por construtor.
  - Remover separadores de diretório escritos manualmente e utilizar `Path.resolve()` ou `Paths.get()`.
  - Não capturar `Exception` genericamente, restringindo o tratamento às exceções esperadas.
  - Desativar recursos de depuração e *SQL logging* antes do ambiente de produção.
  - Utilizar valores booleanos retornados por operações de exclusão ou substituí-las por APIs que informam falhas por exceções.