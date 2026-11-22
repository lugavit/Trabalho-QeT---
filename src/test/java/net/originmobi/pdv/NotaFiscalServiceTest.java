package net.originmobi.pdv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.PessoaService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalTotaisServer;
import net.originmobi.pdv.enumerado.notafiscal.NotaFiscalTipo;
import net.originmobi.pdv.repository.notafiscal.NotaFiscalRepository;
import net.originmobi.pdv.model.Telefone;

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

    private static final String CHAVE_TESTE_XML = "99999999999999999999999999999999999999999999";

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

    @AfterEach
    void limparArquivosTemporarios() throws IOException {
        String contexto = new File(".").getCanonicalPath();
        Path arquivoTeste = Paths.get(contexto + "/src/main/resources/xmlNfe/" + CHAVE_TESTE_XML + ".xml");
        Files.deleteIfExists(arquivoTeste);
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

    // ==================================================
    // NOVOS TESTES INCREMENTADOS (COBERTURA + MUTAÇÃO)
    // ==================================================

    @Test
    void deveCadastrarNotaDeEntradaComFreteFinalidadeDataETotaisZerados() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        when(notasFiscais.buscaUltimaNota(1))
                .thenReturn(42L);

        when(notasFiscais.save(any(NotaFiscal.class)))
                .thenAnswer(invocacao -> {
                    NotaFiscal nota = invocacao.getArgument(0);
                    nota.setCodigo(500L);
                    return nota;
                });

        String codigoGerado = service.cadastrar(
                10L,
                "Compra para comercialização",
                NotaFiscalTipo.ENTRADA);

        assertEquals("500", codigoGerado);

        ArgumentCaptor<NotaFiscal> notaCaptor = ArgumentCaptor.forClass(NotaFiscal.class);
        verify(notasFiscais).save(notaCaptor.capture());
        NotaFiscal notaSalva = notaCaptor.getValue();

        assertEquals(42L, notaSalva.getNumero());
        assertEquals(NotaFiscalTipo.ENTRADA, notaSalva.getTipo());
        assertNotNull(notaSalva.getFreteTipo());
        assertEquals(4L, notaSalva.getFreteTipo().getCodigo());
        assertNotNull(notaSalva.getFinalidade());
        assertEquals(1L, notaSalva.getFinalidade().getCodigo());
        assertEquals(Date.valueOf(LocalDate.now()), notaSalva.getData_cadastro());

        ArgumentCaptor<NotaFiscalTotais> totaisCaptor = ArgumentCaptor.forClass(NotaFiscalTotais.class);
        verify(notaTotais).cadastro(totaisCaptor.capture());
        NotaFiscalTotais totaisSalvos = totaisCaptor.getValue();

        assertEquals(0.0, totaisSalvos.getV_bc(), 0.0001);
        assertEquals(0.0, totaisSalvos.getV_icms(), 0.0001);
        assertEquals(0.0, totaisSalvos.getV_prod(), 0.0001);
        assertEquals(0.0, totaisSalvos.getV_nf(), 0.0001);
    }

    @Test
    void naoDeveChamarTotaisNemSalvarNotaQuandoValidacaoInicialFalhar() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.empty());

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        assertThrows(RuntimeException.class,
                () -> service.cadastrar(10L, "Venda", NotaFiscalTipo.SAIDA));

        verifyNoInteractions(notaTotais);
        verify(notasFiscais, never()).save(any(NotaFiscal.class));
    }

    @Test
    void naoDeveSalvarNotaQuandoCadastroDeTotaisLancarExcecao() {
        when(empresas.verificaEmpresaCadastrada())
                .thenReturn(Optional.of(empresa));

        when(pessoas.buscaPessoa(10L))
                .thenReturn(Optional.of(pessoa));

        doThrow(new RuntimeException("Falha totais"))
                .when(notaTotais).cadastro(any(NotaFiscalTotais.class));

        assertThrows(RuntimeException.class,
                () -> service.cadastrar(10L, "Venda", NotaFiscalTipo.SAIDA));

        verify(notasFiscais, never()).buscaUltimaNota(any(Integer.class));
        verify(notasFiscais, never()).save(any(NotaFiscal.class));
    }

    @Test
    void deveCalcularDigitoVerificadorParaChaveNfe43DigitosComCicloDePesos() {
        // 43 dígitos obrigam o peso a reiniciar de 9 para 2 múltiplas vezes (mata mutantes em peso == 10)
        String chave43Digitos = "3520051234567800019555001000000123100000123";

        Integer dv = service.geraDV(chave43Digitos);

        assertEquals(6, dv);
    }

    @Test
    void deveCalcularDigitoVerificadorQuandoRestoForDiferenteDeZeroEUm() {
        // '2' * 2 = 4 -> resto = 4 -> 11 - 4 = 7
        Integer dv = service.geraDV("2");

        assertEquals(7, dv);
    }

    @Test
    void deveSalvarArquivoXmlNoDiretorioDeNfe() throws IOException {
        String contexto = new File(".").getCanonicalPath();
        Path diretorio = Paths.get(contexto + "/src/main/resources/xmlNfe");
        Files.createDirectories(diretorio);

        String conteudoXml = "<NFe><infNFe Id=\"NFe" + CHAVE_TESTE_XML + "\"/></NFe>";

        service.salvaXML(conteudoXml, CHAVE_TESTE_XML);

        Path arquivoEsperado = diretorio.resolve(CHAVE_TESTE_XML + ".xml");
        assertTrue(Files.exists(arquivoEsperado));

        String conteudoLido = new String(Files.readAllBytes(arquivoEsperado), StandardCharsets.UTF_8);
        assertEquals(conteudoXml, conteudoLido);
    }

    @Test
    void deveRemoverArquivoXmlQuandoArquivoExistir() throws IOException {
        String contexto = new File(".").getCanonicalPath();
        Path diretorio = Paths.get(contexto + "/src/main/resources/xmlNfe");
        Files.createDirectories(diretorio);

        Path arquivoExistente = diretorio.resolve(CHAVE_TESTE_XML + ".xml");
        Files.write(arquivoExistente, "<NFe/>".getBytes(StandardCharsets.UTF_8));
        assertTrue(Files.exists(arquivoExistente));

        service.removeXml(CHAVE_TESTE_XML);

        assertFalse(Files.exists(arquivoExistente));
    }

    @Test
    void naoDeveLancarExcecaoAoRemoverXmlInexistente() {
        service.removeXml("arquivo_inexistente_999999999");
    }

    @Test
    void deveEmitirNotaFiscalGerandoChaveESalvandoNoRepositorio() throws IOException {
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

        Endereco enderecoEmissor = new Endereco();
        enderecoEmissor.setRua("Rua Teste");
        enderecoEmissor.setNumero("100");
        enderecoEmissor.setBairro("Centro");
        enderecoEmissor.setCep("24020-000");
        enderecoEmissor.setCidade(cidade);

        RegimeTributario regime = new RegimeTributario();
        regime.setTipoRegime(1);

        Empresa emissorCompleto = new Empresa(
                "Empresa Teste LTDA",
                "Empresa Teste",
                "12345678000195",
                "12345678",
                regime,
                enderecoEmissor,
                parametro);

        Endereco enderecoDest = new Endereco();
        enderecoDest.setRua("Av Destinatario");
        enderecoDest.setNumero("200");
        enderecoDest.setBairro("Icaraí");
        enderecoDest.setCep("24220-000");
        enderecoDest.setCidade(cidade);

        Telefone telefone = new Telefone();
        telefone.setFone("21999999999");

        Pessoa destinatarioCompleto = new Pessoa();
        destinatarioCompleto.setCodigo(10L);
        destinatarioCompleto.setNome("Cliente Completo");
        destinatarioCompleto.setCpfcnpj("12345678909");
        destinatarioCompleto.setEndereco(enderecoDest);
        destinatarioCompleto.setTelefone(Collections.singletonList(telefone));

        FreteTipo frete = new FreteTipo();
        frete.setCodigo(4L);
        frete.setTipo(9);

        NotaFiscalFinalidade finalidade = new NotaFiscalFinalidade();
        finalidade.setCodigo(1L);
        finalidade.setTipo(1);

        NotaFiscalTotais totais = new NotaFiscalTotais(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        NotaFiscal nota = new NotaFiscal(
                1L,
                55,
                NotaFiscalTipo.SAIDA,
                "Venda de mercadoria",
                1,
                emissorCompleto,
                destinatarioCompleto,
                1,
                "0.0.1-beta",
                frete,
                finalidade,
                totais,
                2,
                Date.valueOf(LocalDate.now()));
        nota.setCodigo(1L);
        nota.setItens(Collections.emptyList());

        assertThrows(NullPointerException.class, () -> service.emitir(nota));

        Files.deleteIfExists(Paths.get(contexto + "/src/main/resources/xmlNfe/" + nota.getChave_acesso() + ".xml"));
    }
}
