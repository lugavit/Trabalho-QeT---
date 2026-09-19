# AI Log

Aluno: Lucas dos Santos Cabral da Silva 


## Registro 01

- **Data:** 13/09/2026
- **Ferramenta:** Deepseek-v4.1-flash
- **Objetivo:** teste unitário JUnit4 + mockito - da classe `EmpresaService
- **Prompt:** "teste unitário JUnit4 + mockito - da classe `EmpresaService`, idetifique se é necessario usar mockito e justifique para que cada teste foi criado, se for necessário envio as dependências da classe"
- **Resultado:** A IA apresentou sugestões  testes unitários para três métodos da classe EmpresaService 
- **Uso no projeto:**. `@Mock` → cria um "boneco" de cada dependência ; `@InjectMocks` → injeta os 5 bonecos dentro da `EmpresaService`; A anotação `@RunWith(MockitoJUnitRunner.class)`;  diz ao JUnit 4 que quem manda no teste é o Mockito. Sem isso, os `@Mock` e `@InjectMocks` não funcionam. também foi utilizado **`when(...)`**   **`verify(...)`** **`doThrow(...)`**  o primeiro trata o comportamento do "boneco" o segundo confirma que o método foi chamado já o terceiro configura para o boneco lançar a exceção.
- **Modificações:** O conteúdo foi revisado nos casos 13 e 14,Eu **não coloquei testes para eles** porque eles vão **lançar `NoSuchElementException` não tratada**

## Registro 02

- **Data:** 17/09/2026
- **Ferramenta:** Deepseek-v4.1-flash
- **Objetivo:** ter mais clareza no código e corrigir warning do compilador java
- **Prompt:** compilou e executou o teste unitario no eclipse contudo o compilador reclama dessa notação  - verify(parametros, times(1)).save(any(EmpresaParametro.class))                                                    when(parametros.save(any(EmpresaParametro.class)) - when(empresas.save(any(Empresa.class))) - when(empresas.save(any(Empresa.class))) - deixe o código mais legivel e facil de entender por favor (refatore o código) respeite o que orientei sobre o projeto - (use esse package e imports )

- **Resultado:** melhoria na sintaxe que facilita a compreensão para quem vai ler o código no futuro.
- **Uso no projeto:**o teste do método cadastro(empresa); verificaempresacadastrada() ; merger() -> codigo != null {tem os respectivos cenários - > atualização com sucesso (os 3 updates funcionam; update de empresa falha. update de parâmetros falha; o update de endereço falha.} merger() - > codigo == null {cadastro de empresa nova com sucesso.  falha ao cadastrar o endereço. falha ao salvar a empresa no final do fluxo.}
- **Modificações:** pradonizar o uso `doThrow(...).when(...)`. Quando você faz `any(Empresa.class)`, o compilador fica na dúvida sobre qual tipo está sendo usado. o `save` é **genérico** — ele aceita qualquer coisa que estenda `T`. Quando você faz `any(Empresa.class)`, o compilador fica na dúvida sobre qual tipo está sendo usado. então resolvemos erros na compilação.


