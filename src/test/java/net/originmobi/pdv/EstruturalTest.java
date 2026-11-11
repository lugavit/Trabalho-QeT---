package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.originmobi.pdv.enumerado.notafiscal.NotaFiscalTipo;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.NotaFiscal;
import net.originmobi.pdv.model.NotaFiscalTotais;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.repository.notafiscal.NotaFiscalRepository;
import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.PessoaService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalTotaisServer;

public class EstruturalTest {

    @InjectMocks
    private NotaFiscalService notaFiscalService;

    @Mock
    private NotaFiscalRepository notasFiscais;

    @Mock
    private EmpresaService empresas;

    @Mock
    private NotaFiscalTotaisServer notaTotais;

    @Mock
    private PessoaService pessoas;

    private Empresa empresaValida;
    private EmpresaParametro parametroValido;
    private Pessoa pessoaValida;

    @Before
    public void setUp() {
        // Inicializa manualmente os mocks e faz a injeção no 'notaFiscalService'
        MockitoAnnotations.initMocks(this);

        parametroValido = new EmpresaParametro();
        parametroValido.setSerie_nfe(1);
        parametroValido.setAmbiente(1);

        empresaValida = new Empresa();
        empresaValida.setParametro(parametroValido);

        pessoaValida = new Pessoa();
        pessoaValida.setCodigo(100L);
    }

    // =========================================================================
    // SEÇÃO 1: TESTE ESTRUTURAL DO MÉTODO 'cadastrar' (CRITÉRIO TODA-ARESTAS)
    // =========================================================================

    @Test
    public void testCadastrar_CT01_EmpresaNaoCadastrada() {
        when(empresas.verificaEmpresaCadastrada()).thenReturn(Optional.empty());

        try {
            notaFiscalService.cadastrar(100L, "Venda", NotaFiscalTipo.SAIDA);
            fail("Deveria ter lançado exceção de empresa não cadastrada");
        } catch (RuntimeException e) {
            assertEquals("Nenhuma empresa cadastrada, verifique", e.getMessage());
        }
    }

    @Test
    public void testCadastrar_CT02_PessoaNaoEncontrada() {
        when(empresas.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresaValida));
        when(pessoas.buscaPessoa(100L)).thenReturn(Optional.empty());

        try {
            notaFiscalService.cadastrar(100L, "Venda", NotaFiscalTipo.SAIDA);
            fail("Deveria ter lançado exceção de destinatário não selecionado");
        } catch (RuntimeException e) {
            assertEquals("Favor, selecione o destinatário", e.getMessage());
        }
    }

    @Test
    public void testCadastrar_CT03_SerieInvalida() {
        parametroValido.setSerie_nfe(0);
        when(empresas.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresaValida));
        when(pessoas.buscaPessoa(100L)).thenReturn(Optional.of(pessoaValida));

        try {
            notaFiscalService.cadastrar(100L, "Venda", NotaFiscalTipo.SAIDA);
            fail("Deveria ter lançado exceção de série não cadastrada");
        } catch (RuntimeException e) {
            assertEquals("Não existe série cadastrada para o modelo 55, verifique", e.getMessage());
        }
    }

    @Test
    public void testCadastrar_CT04_ErroCadastroTotais() {
        when(empresas.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresaValida));
        when(pessoas.buscaPessoa(100L)).thenReturn(Optional.of(pessoaValida));
        doThrow(new RuntimeException("Erro BD")).when(notaTotais).cadastro(any(NotaFiscalTotais.class));

        try {
            notaFiscalService.cadastrar(100L, "Venda", NotaFiscalTipo.SAIDA);
            fail("Deveria ter capturado a exceção no cadastro de totais");
        } catch (RuntimeException e) {
            assertEquals("Erro ao cadastrar a nota, chame o suporte", e.getMessage());
        }
    }

    @Test
    public void testCadastrar_CT05_ErroSalvarNota() {
        when(empresas.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresaValida));
        when(pessoas.buscaPessoa(100L)).thenReturn(Optional.of(pessoaValida));
        when(notasFiscais.buscaUltimaNota(1)).thenReturn(10L);
        when(notasFiscais.save(any(NotaFiscal.class))).thenThrow(new RuntimeException("Erro BD"));

        try {
            notaFiscalService.cadastrar(100L, "Venda", NotaFiscalTipo.SAIDA);
            fail("Deveria ter capturado a exceção no save da nota");
        } catch (RuntimeException e) {
            assertEquals("Erro ao cadastrar a nota, chame o suporte", e.getMessage());
        }
    }

    @Test
    public void testCadastrar_CT06_Sucesso() {
        when(empresas.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresaValida));
        when(pessoas.buscaPessoa(100L)).thenReturn(Optional.of(pessoaValida));
        when(notasFiscais.buscaUltimaNota(1)).thenReturn(10L);

        NotaFiscal notaSalva = new NotaFiscal();
        notaSalva.setCodigo(500L);
        when(notasFiscais.save(any(NotaFiscal.class))).thenReturn(notaSalva);

        String codigoRetornado = notaFiscalService.cadastrar(100L, "Venda", NotaFiscalTipo.SAIDA);

        assertEquals("500", codigoRetornado);
        verify(notasFiscais).save(any(NotaFiscal.class));
    }

    // =========================================================================
    // SEÇÃO 2: TESTE ESTRUTURAL DO MÉTODO 'geraDV' (CURTO-CIRCUITO a || b)
    // =========================================================================

    @Test
    public void testGeraDV_CT01_ExcecaoCatch_StringNull() {
        Integer dv = notaFiscalService.geraDV(null);
        assertEquals(Integer.valueOf(0), dv);
    }

    @Test
    public void testGeraDV_CT02_CurtoCircuito_RestoZero() {
        Integer dv = notaFiscalService.geraDV("");
        assertEquals(Integer.valueOf(0), dv);
    }

    @Test
    public void testGeraDV_CT03_RestoUm() {
        Integer dv = notaFiscalService.geraDV("6"); 
        assertEquals(Integer.valueOf(0), dv);
    }

    @Test
    public void testGeraDV_CT04_RestoMaiorQueUm() {
        Integer dv = notaFiscalService.geraDV("1");
        assertEquals(Integer.valueOf(9), dv);
    }

    // =========================================================================
    // SEÇÃO 3: TESTES UNITÁRIOS CONVENCIONAIS DOS OUTROS MÉTODOS
    // =========================================================================

    @Test
    public void testLista() {
        when(notasFiscais.findAll()).thenReturn(Collections.singletonList(new NotaFiscal()));

        List<NotaFiscal> resultado = notaFiscalService.lista();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(notasFiscais).findAll();
    }

    @Test
    public void testBusca() {
        NotaFiscal nota = new NotaFiscal();
        nota.setCodigo(1L);
        when(notasFiscais.findById(1L)).thenReturn(Optional.of(nota));

        Optional<NotaFiscal> resultado = notaFiscalService.busca(1L);

        assertEquals(true, resultado.isPresent());
        assertEquals(Long.valueOf(1L), resultado.get().getCodigo());
        verify(notasFiscais).findById(1L);
    }

    @Test
    public void testTotalNotaFiscalEmitidas() {
        when(notasFiscais.totalNotaFiscalEmitidas()).thenReturn(42);

        int total = notaFiscalService.totalNotaFiscalEmitidas();

        assertEquals(42, total);
        verify(notasFiscais).totalNotaFiscalEmitidas();
    }
}
