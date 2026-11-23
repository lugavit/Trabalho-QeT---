package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.originmobi.pdv.enumerado.notafiscal.NotaFiscalTipo;
import net.originmobi.pdv.model.Cidade;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.Endereco;
import net.originmobi.pdv.model.Estado;
import net.originmobi.pdv.model.FreteTipo;
import net.originmobi.pdv.model.NotaFiscal;
import net.originmobi.pdv.model.NotaFiscalFinalidade;
import net.originmobi.pdv.model.NotaFiscalTotais;
import net.originmobi.pdv.model.Pais;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.model.RegimeTributario;
import net.originmobi.pdv.repository.notafiscal.NotaFiscalRepository;
import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.PessoaService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalTotaisServer;
import net.originmobi.pdv.model.Telefone;

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

    private static final String CHAVE_ESTRUTURAL_XML = "88888888888888888888888888888888888888888888";

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

    @After
    public void tearDown() throws IOException {
        String contexto = new File(".").getCanonicalPath();
        Files.deleteIfExists(Paths.get(contexto + "/src/main/resources/xmlNfe/" + CHAVE_ESTRUTURAL_XML + ".xml"));
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

    // =========================================================================
    // SEÇÃO 4: INCREMENTO ESTRUTURAL (ARESTAS FALTANTES EM geraDV, XML E emitir)
    // =========================================================================

    @Test
    public void testGeraDV_CT05_ArestaResetPesoIgualA10_Chave9Digitos() {
        // Cobre explicitamente a aresta verdadeira de: if (peso == 10) { peso = 2; }
        // No CT03 ("6") o laço rodava apenas 1 vez (peso=2->3), deixando a aresta peso==10 descoberta.
        // Com 9 dígitos ("123456789"), na 8ª iteração peso atinge 10 e reinicia para 2 na 9ª iteração.
        Integer dv = notaFiscalService.geraDV("123456789");
        assertEquals(Integer.valueOf(7), dv);
    }

    @Test
    public void testSalvaXML_CT01_SucessoGravacaoArquivo() throws IOException {
        // Cobre a aresta normal do bloco try de gravação de arquivo XML
        String contexto = new File(".").getCanonicalPath();
        Path diretorio = Paths.get(contexto + "/src/main/resources/xmlNfe");
        Files.createDirectories(diretorio);

        String xmlConteudo = "<NFe><infNFe Id=\"NFe" + CHAVE_ESTRUTURAL_XML + "\"/></NFe>";
        notaFiscalService.salvaXML(xmlConteudo, CHAVE_ESTRUTURAL_XML);

        Path arquivoSalvo = diretorio.resolve(CHAVE_ESTRUTURAL_XML + ".xml");
        assertTrue(Files.exists(arquivoSalvo));
        assertEquals(xmlConteudo, new String(Files.readAllBytes(arquivoSalvo), StandardCharsets.UTF_8));
    }

    @Test
    public void testSalvaXML_CT02_ArestaCatchIOException_CaminhoInvalido() throws IOException {
        // Cobre a aresta de exceção catch (IOException e) forçando subdiretório inexistente na chave
        String contexto = new File(".").getCanonicalPath();
        Files.createDirectories(Paths.get(contexto + "/src/main/resources/xmlNfe"));

        notaFiscalService.salvaXML("<NFe/>", "subpasta_inexistente_999/chave_invalida");
    }

    @Test
    public void testRemoveXml_CT01_ArestaTrue_ArquivoExistente() throws IOException {
        // Cobre a aresta verdadeira de: if (file.exists()) { file.delete(); }
        String contexto = new File(".").getCanonicalPath();
        Path diretorio = Paths.get(contexto + "/src/main/resources/xmlNfe");
        Files.createDirectories(diretorio);

        Path arquivoExistente = diretorio.resolve(CHAVE_ESTRUTURAL_XML + ".xml");
        Files.write(arquivoExistente, "<NFe/>".getBytes(StandardCharsets.UTF_8));
        assertTrue(Files.exists(arquivoExistente));

        notaFiscalService.removeXml(CHAVE_ESTRUTURAL_XML);

        assertFalse(Files.exists(arquivoExistente));
    }

    @Test
    public void testRemoveXml_CT02_ArestaFalse_ArquivoInexistente() {
        // Cobre a aresta falsa de: if (file.exists())
        notaFiscalService.removeXml("chave_inexistente_estrutural_000");
    }

    @Test
    public void testBusca_CT02_NotaNaoEncontrada() {
        when(notasFiscais.findById(999L)).thenReturn(Optional.empty());

        Optional<NotaFiscal> resultado = notaFiscalService.busca(999L);

        assertFalse(resultado.isPresent());
        verify(notasFiscais).findById(999L);
    }

    @Test
    public void testEmitir_CT01_CaminhoCompletoEmissao() throws IOException {
        // Cobre todas as instruções sequenciais do método emitir(NotaFiscal)
        String contexto = new File(".").getCanonicalPath();
        Files.createDirectories(Paths.get(contexto + "/src/main/resources/xmlNfe"));

        Pais pais = new Pais();
        pais.setCodigo_pais("1058");
        pais.setNome("Brasil");

        Estado estado = new Estado();
        estado.setSigla("RJ");
        estado.setCodigoUF("33");
        estado.setPais(pais);

        Cidade cidade = new Cidade();
        cidade.setNome("Niterói");
        cidade.setCodigo_municipio("3303302");
        cidade.setEstado(estado);

        Endereco endEmissor = new Endereco();
        endEmissor.setRua("Rua Emissor");
        endEmissor.setNumero("10");
        endEmissor.setBairro("Centro");
        endEmissor.setCep("24020-000");
        endEmissor.setCidade(cidade);

        RegimeTributario regime = new RegimeTributario();
        regime.setTipoRegime(1);

        Empresa emissor = new Empresa(
                "Empresa Estrutural LTDA",
                "Empresa Estrutural",
                "12345678000195",
                "12345678",
                regime,
                endEmissor,
                parametroValido);

        Endereco endDest = new Endereco();
        endDest.setRua("Rua Destinatario");
        endDest.setNumero("20");
        endDest.setBairro("Icaraí");
        endDest.setCep("24220-000");
        endDest.setCidade(cidade);

        Pessoa destinatario = new Pessoa();
        destinatario.setCodigo(100L);
        destinatario.setNome("Destinatario Teste");
        destinatario.setCpfcnpj("12345678909");
        destinatario.setEndereco(endDest);

        FreteTipo frete = new FreteTipo();
        frete.setCodigo(4L);

        NotaFiscalFinalidade finalidade = new NotaFiscalFinalidade();
        finalidade.setCodigo(1L);

        NotaFiscalTotais totais = new NotaFiscalTotais(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        NotaFiscal nota = new NotaFiscal(
                1L,
                55,
                NotaFiscalTipo.SAIDA,
                "Venda de mercadoria",
                1,
                emissor,
                destinatario,
                1,
                "0.0.1-beta",
                frete,
                finalidade,
                totais,
                1,
                Date.valueOf(LocalDate.now()));
        nota.setCodigo(1L);
        nota.setItens(Collections.emptyList());

        try {
            notaFiscalService.emitir(nota);
            fail("Deveria lançar NullPointerException por ausência de certificado digital no AssinaXML");
        } catch (NullPointerException expected) {
            // Cobre o fluxo de entrada do método emitir() sem certificado .pfx no ambiente unitário
        }

        Files.deleteIfExists(Paths.get(contexto + "/src/main/resources/xmlNfe/" + nota.getChave_acesso() + ".xml"));
    }
}
