package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.originmobi.pdv.model.Cidade;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.Endereco;
import net.originmobi.pdv.model.RegimeTributario;
import net.originmobi.pdv.repository.EmpresaParametrosRepository;
import net.originmobi.pdv.repository.EmpresaRepository;
import net.originmobi.pdv.service.CidadeService;
import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.EnderecoService;
import net.originmobi.pdv.service.RegimeTributarioService;

/**
 * Testes unitários da classe EmpresaService.
 *
 * A ideia aqui é testar só a lógica da EmpresaService, sem subir
 * Spring, banco de dados ou qualquer coisa externa. Para isso, usamos
 * mocks (Mockito) das 5 dependências que a Service injeta.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmpresaServiceTest {

    // ---------------------------------------------------------
    // Dependências simuladas (mocks)
    // Cada uma "finge" ser a classe real para isolar a Service
    // ---------------------------------------------------------

    @Mock
    private EmpresaRepository empresas;

    @Mock
    private EmpresaParametrosRepository parametros;

    @Mock
    private RegimeTributarioService regimes;

    @Mock
    private CidadeService cidades;

    @Mock
    private EnderecoService enderecos;

    // ---------------------------------------------------------
    // Classe sob teste
    // O Mockito injeta os 5 mocks acima dentro dela
    // ---------------------------------------------------------

    @InjectMocks
    private EmpresaService service;

    // =========================================================
    // TESTES DO MÉTODO cadastro(Empresa)
    // =========================================================

    /**
     * Cenário: chamada normal de cadastro.
     * Esperado: o repository.save é chamado 1 vez com a empresa.
     */
    @Test
    public void deveSalvarEmpresaComSucesso() {
        Empresa empresa = new Empresa();

        service.cadastro(empresa);

        verify(empresas, times(1)).save(empresa);
    }

    /**
     * Cenário: o repository.save lança exceção.
     * Esperado: a Service NÃO propaga a exceção (ela engole e imprime no console).
     * Como o método não retorna nada, o próprio JUnit já falha se a exceção vazar.
     */
    @Test
    public void naoDevePropagarExcecaoQuandoSaveFalha() {
        Empresa empresa = new Empresa();

        doThrow(new RuntimeException("erro no banco"))
                .when(empresas).save(any(Empresa.class));

        service.cadastro(empresa);

        verify(empresas, times(1)).save(empresa);
    }

    // =========================================================
    // TESTES DO MÉTODO verificaEmpresaCadastrada()
    // =========================================================

    /**
     * Cenário: existe uma empresa cadastrada no repositório.
     * Esperado: a Service devolve o Optional com a empresa.
     */
    @Test
    public void deveRetornarEmpresaQuandoExiste() {
        Empresa empresa = new Empresa();

        when(empresas.buscaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        Optional<Empresa> resultado = service.verificaEmpresaCadastrada();

        assertTrue("Deveria ter retornado uma empresa", resultado.isPresent());
        assertEquals(empresa, resultado.get());
    }

    /**
     * Cenário: não existe empresa cadastrada.
     * Esperado: a Service devolve um Optional vazio.
     */
    @Test
    public void deveRetornarVazioQuandoNaoExiste() {
        when(empresas.buscaEmpresaCadastrada())
                .thenReturn(Optional.empty());

        Optional<Empresa> resultado = service.verificaEmpresaCadastrada();

        assertFalse("Não deveria ter retornado empresa", resultado.isPresent());
    }

    // =========================================================
    // TESTES DO MÉTODO merger(...) — RAMO DE ATUALIZAÇÃO (codigo != null)
    // =========================================================

    /**
     * Cenário: atualização com sucesso (os 3 updates funcionam).
     * Esperado: retorno "Empresa salva com sucesso" e os 3 updates chamados.
     */
    @Test
    public void deveAtualizarEmpresaComSucesso() {
        String resultado = chamarMergerComCodigo();

        assertEquals("Empresa salva com sucesso", resultado);

        verify(empresas, times(1)).update(
                anyLong(), anyString(), anyString(),
                anyString(), anyString(), anyLong());

        verify(parametros, times(1)).update(
                anyInt(), anyInt(), any());

        verify(enderecos, times(1)).update(
                anyLong(), anyLong(), anyString(),
                anyString(), anyString(), anyString(), anyString());
    }

    /**
     * Cenário: o update de empresa falha.
     * Esperado: retorno de mensagem de erro.
     */
    @Test
    public void deveRetornarErroQuandoUpdateEmpresaFalha() {
        doThrow(new RuntimeException("erro"))
                .when(empresas).update(
                        anyLong(), anyString(), anyString(),
                        anyString(), anyString(), anyLong());

        String resultado = chamarMergerComCodigo();

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    /**
     * Cenário: o update de parâmetros falha.
     * Esperado: retorno de mensagem de erro.
     */
    @Test
    public void deveRetornarErroQuandoUpdateParametrosFalha() {
        doThrow(new RuntimeException("erro"))
                .when(parametros).update(anyInt(), anyInt(), any());

        String resultado = chamarMergerComCodigo();

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    /**
     * Cenário: o update de endereço falha.
     * Esperado: retorno de mensagem de erro.
     */
    @Test
    public void deveRetornarErroQuandoUpdateEnderecoFalha() {
        doThrow(new RuntimeException("erro"))
                .when(enderecos).update(
                        anyLong(), anyLong(), anyString(),
                        anyString(), anyString(), anyString(), anyString());

        String resultado = chamarMergerComCodigo();

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    // =========================================================
    // TESTES DO MÉTODO merger(...) — RAMO DE CADASTRO (codigo == null)
    // =========================================================

    /**
     * Cenário: cadastro de empresa nova com sucesso.
     * Esperado: retorno "Empresa salva com sucesso" e os 3 saves chamados.
     */
    @Test
    public void deveCadastrarEmpresaComSucesso() {
        prepararBuscaDeRegimeECidade();

        String resultado = chamarMergerSemCodigo();

        assertEquals("Empresa salva com sucesso", resultado);

        verify(parametros, times(1)).save(any(EmpresaParametro.class));
        verify(enderecos, times(1)).cadastrar(any(Endereco.class));
        verify(empresas, times(1)).save(any(Empresa.class));
    }

    /**
     * Cenário: falha ao salvar os parâmetros da empresa.
     * Esperado: retorno de mensagem de erro.
     */
    @Test
    public void deveRetornarErroQuandoSaveParametroFalha() {
        doThrow(new RuntimeException("erro"))
                .when(parametros).save(any(EmpresaParametro.class));

        String resultado = chamarMergerSemCodigo();

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    /**
     * Cenário: falha ao cadastrar o endereço.
     * Esperado: retorno de mensagem de erro.
     */
    @Test
    public void deveRetornarErroQuandoCadastrarEnderecoFalha() {
        prepararBuscaDeRegimeECidade();

        doThrow(new RuntimeException("erro"))
                .when(enderecos).cadastrar(any(Endereco.class));

        String resultado = chamarMergerSemCodigo();

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    /**
     * Cenário: falha ao salvar a empresa no final do fluxo.
     * Esperado: retorno de mensagem de erro.
     */
    @Test
    public void deveRetornarErroQuandoSaveEmpresaFalha() {
        prepararBuscaDeRegimeECidade();

        doThrow(new RuntimeException("erro"))
                .when(empresas).save(any(Empresa.class));

        String resultado = chamarMergerSemCodigo();

        assertEquals("Erro ao salvar dados da empresa, chame o suporte", resultado);
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // Deixam os testes mais curtos e evitam repetição
    // =========================================================

    /**
     * Chama o merger passando um código (ramo de atualização).
     */
    private String chamarMergerComCodigo() {
        return service.merger(
                1L,              // codigo
                "nome",          // nome
                "fantasia",      // nome_fantasia
                "cnpj",          // cnpj
                "ie",            // ie
                1,               // serie
                1,               // ambiente
                1L,              // codRegime
                1L,              // codendereco
                1L,              // codcidade
                "rua",           // rua
                "bairro",        // bairro
                "1",             // numero
                "cep",           // cep
                "ref",           // referencia
                10.0             // aliqCalcCredito
        );
    }

    /**
     * Chama o merger passando null no código (ramo de cadastro novo).
     */
    private String chamarMergerSemCodigo() {
        return service.merger(
                null,            // codigo (null = cadastro novo)
                "nome",
                "fantasia",
                "cnpj",
                "ie",
                1,
                1,
                1L,
                1L,
                1L,
                "rua",
                "bairro",
                "1",
                "cep",
                "ref",
                10.0
        );
    }

    /**
     * Configura os mocks de regime e cidade para devolver valores válidos.
     * Sem isso, o código chama .get() em Optional vazio e lança exceção.
     */
    private void prepararBuscaDeRegimeECidade() {
        when(regimes.busca(1L)).thenReturn(Optional.of(new RegimeTributario()));
        when(cidades.busca(1L)).thenReturn(Optional.of(new Cidade()));
    }
}