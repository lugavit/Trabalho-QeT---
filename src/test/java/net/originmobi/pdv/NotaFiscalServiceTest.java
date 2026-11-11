package net.originmobi.pdv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.NotaFiscal;
import net.originmobi.pdv.model.NotaFiscalTotais;
import net.originmobi.pdv.model.Pessoa;

import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.PessoaService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalTotaisServer;
import net.originmobi.pdv.enumerado.notafiscal.NotaFiscalTipo;
import net.originmobi.pdv.repository.notafiscal.NotaFiscalRepository;


class NotaFiscalServiceTest {

    @Mock
    private NotaFiscalRepository notasFiscais;

    @Mock
    private EmpresaService empresas;

    @Mock
    private NotaFiscalTotaisServer notaTotais;

    @Mock
    private PessoaService pessoas;

    @InjectMocks
    private NotaFiscalService service;

    private Empresa empresa;
    private Pessoa pessoa;
    private EmpresaParametro parametro;

   @BeforeEach
      void configurar() {
        MockitoAnnotations.initMocks(this);

       parametro = new EmpresaParametro();
       parametro.setSerie_nfe(1);
       parametro.setAmbiente(2);

       empresa = new Empresa(
            null, null, null, null, null, null, parametro);

       pessoa = new Pessoa();
       pessoa.setCodigo(10L);
       pessoa.setNome("Cliente de teste");
}

    // --------------------------------------------------
    // lista()
    // --------------------------------------------------

    @Test
    void deveRetornarListaDeNotasFiscais() {
        NotaFiscal nota = new NotaFiscal();

        List<NotaFiscal> listaEsperada = Collections.singletonList(nota);

        when(notasFiscais.findAll()).thenReturn(listaEsperada);

        List<NotaFiscal> resultado = service.lista();

        assertEquals(1, resultado.size());
        assertSame(nota, resultado.get(0));

        verify(notasFiscais).findAll();
    }

    // --------------------------------------------------
    // busca()
    // --------------------------------------------------

    @Test
    void deveBuscarNotaFiscalPeloCodigo() {
        NotaFiscal nota = new NotaFiscal();

        when(notasFiscais.findById(1L))
                .thenReturn(Optional.of(nota));

        Optional<NotaFiscal> resultado = service.busca(1L);

        assertNotNull(resultado);
        assertEquals(true, resultado.isPresent());
        assertSame(nota, resultado.get());

        verify(notasFiscais).findById(1L);
    }

    @Test
    void deveRetornarOptionalVazioQuandoNotaNaoExistir() {
        when(notasFiscais.findById(99L))
                .thenReturn(Optional.empty());

        Optional<NotaFiscal> resultado = service.busca(99L);

        assertEquals(false, resultado.isPresent());

        verify(notasFiscais).findById(99L);
    }

    // --------------------------------------------------
    // totalNotaFiscalEmitidas()
    // --------------------------------------------------

    @Test
    void deveRetornarQuantidadeDeNotasEmitidas() {
        when(notasFiscais.totalNotaFiscalEmitidas()).thenReturn(15);

        Integer resultado = service.totalNotaFiscalEmitidas();

        assertEquals(15, resultado);

        verify(notasFiscais).totalNotaFiscalEmitidas();
    }

    @Test
    void deveRetornarZeroQuandoNaoExistiremNotasEmitidas() {
        when(notasFiscais.totalNotaFiscalEmitidas()).thenReturn(0);

        Integer resultado = service.totalNotaFiscalEmitidas();

        assertEquals(0, resultado);
    }

    // --------------------------------------------------
    // cadastrar()
    // --------------------------------------------------

    @Test
    void deveLancarExcecaoQuandoNaoExistirEmpresaCadastrada() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.empty());

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(
                        10L, "Venda de mercadoria", NotaFiscalTipo.SAIDA));

        assertEquals(
                "Nenhuma empresa cadastrada, verifique",
                excecao.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoDestinatarioNaoExistir() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(99L))
                .thenReturn(Optional.empty());

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(
                        99L, "Venda de mercadoria", NotaFiscalTipo.SAIDA));

        assertEquals(
                "Favor, selecione o destinatário",
                excecao.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoSerieDaNotaForZero() {
        parametro.setSerie_nfe(0);

        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(
                        10L, "Venda de mercadoria", NotaFiscalTipo.SAIDA));

        assertEquals(
                "Não existe série cadastrada para o modelo 55, verifique",
                excecao.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCadastroDosTotaisFalhar() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        doThrow(new RuntimeException("Erro no cadastro dos totais"))
                .when(notaTotais).cadastro(any(NotaFiscalTotais.class));

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(
                        10L, "Venda de mercadoria", NotaFiscalTipo.SAIDA));

        assertEquals(
                "Erro ao cadastrar a nota, chame o suporte",
                excecao.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoBuscaDaUltimaNotaFalhar() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        when(notasFiscais.buscaUltimaNota(1))
                .thenThrow(new RuntimeException("Erro no banco"));

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(
                        10L, "Venda de mercadoria", NotaFiscalTipo.SAIDA));

        assertEquals(
                "Erro ao cadastrar a nota, chame o suporte",
                excecao.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoSalvamentoDaNotaFalhar() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        when(notasFiscais.buscaUltimaNota(1))
                .thenReturn(1L);

        when(notasFiscais.save(any(NotaFiscal.class)))
                .thenThrow(new RuntimeException("Erro ao salvar"));

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> service.cadastrar(
                        10L, "Venda de mercadoria", NotaFiscalTipo.SAIDA));

        assertEquals(
                "Erro ao cadastrar a nota, chame o suporte",
                excecao.getMessage());
    }

    @Test
    void deveCadastrarNotaFiscalComSucesso() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        when(notasFiscais.buscaUltimaNota(1))
                .thenReturn(1L);

        when(notasFiscais.save(any(NotaFiscal.class)))
                .thenAnswer(invocacao -> {
                    NotaFiscal nota = invocacao.getArgument(0);
                    nota.setCodigo(123L);
                    return nota;
                });

        String resultado = service.cadastrar(
                10L,
                "Venda de mercadoria",
                NotaFiscalTipo.SAIDA);

        assertEquals("123", resultado);

        ArgumentCaptor<NotaFiscal> captor =
                ArgumentCaptor.forClass(NotaFiscal.class);

        verify(notasFiscais).save(captor.capture());

        NotaFiscal notaSalva = captor.getValue();

        assertEquals(1L, notaSalva.getNumero());
        assertEquals(55, notaSalva.getModelo());
        assertEquals(1, notaSalva.getSerie());
        assertEquals(NotaFiscalTipo.SAIDA, notaSalva.getTipo());
        assertEquals("Venda de mercadoria", notaSalva.getNatureza_operacao());
        assertSame(empresa, notaSalva.getEmissor());
        assertSame(pessoa, notaSalva.getDestinatario());
        assertEquals(1, notaSalva.getTipo_emissao());
        assertEquals("0.0.1-beta", notaSalva.getVerProc());
        assertEquals(2, notaSalva.getTipo_ambiente());

        assertNotNull(notaSalva.getTotais());

        ArgumentCaptor<NotaFiscalTotais> totaisCaptor =
                ArgumentCaptor.forClass(NotaFiscalTotais.class);

        verify(notaTotais).cadastro(totaisCaptor.capture());

        assertNotNull(totaisCaptor.getValue());
    }

    // --------------------------------------------------
    // geraDV()
    // --------------------------------------------------

    @Test
    void deveCalcularDigitoVerificadorCorretamente() {
        Integer resultado = service.geraDV("123456789");

        assertEquals(7, resultado);
    }

    @Test
    void deveRetornarZeroQuandoRestoDaDivisaoForZero() {
        Integer resultado = service.geraDV("0");

        assertEquals(0, resultado);
    }

    @Test
    void deveRetornarZeroQuandoRestoDaDivisaoForUm() {
        Integer resultado = service.geraDV("6");

        assertEquals(0, resultado);
    }

    @Test
    void deveRetornarZeroParaCodigoVazio() {
        Integer resultado = service.geraDV("");

        assertEquals(0, resultado);
    }

    @Test
    void deveRetornarZeroQuandoCodigoForNulo() {
        Integer resultado = service.geraDV(null);

        assertEquals(0, resultado);
    }
}
