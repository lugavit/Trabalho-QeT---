package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;
import net.originmobi.pdv.service.CaixaLancamentoService;
import net.originmobi.pdv.service.CaixaService;
import net.originmobi.pdv.service.UsuarioService;

@RunWith(MockitoJUnitRunner.class)
public class CaixaServiceTest {

	@InjectMocks
	private CaixaService caixaService;

	@Mock
	private CaixaRepository caixas;

	@Mock
	private UsuarioService usuarios;

	@Mock
	private CaixaLancamentoService lancamentos;

	private Usuario usuarioMock;
	private BCryptPasswordEncoder encoder;

	@Before
	public void setUp() {
		encoder = new BCryptPasswordEncoder();
		usuarioMock = new Usuario();
		usuarioMock.setCodigo(1L);
		usuarioMock.setSenha(encoder.encode("senha123"));

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("usuarioTeste", "senha123"));

		lenient().when(usuarios.buscaUsuario(any())).thenReturn(usuarioMock);
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testConstrutorPadrao() {
		CaixaService construtorVazio = new CaixaService();
		assertNotNull(construtorVazio);
	}

	@Test
	public void testCadastro_CaixaComSucesso_ValorMaiorQueZero() {
		Caixa caixa = new Caixa();
		caixa.setCodigo(10L);
		caixa.setTipo(CaixaTipo.CAIXA);
		caixa.setDescricao("");
		caixa.setValor_abertura(100.0);

		when(caixas.caixaAberto()).thenReturn(Optional.empty());

		Long codigoGerado = caixaService.cadastro(caixa);

		assertEquals("Caixa diário", caixa.getDescricao());
		assertEquals(Double.valueOf(100.0), caixa.getValor_abertura());
		assertSame(usuarioMock, caixa.getUsuario());
		assertNotNull(caixa.getData_cadastro());
		assertEquals(Long.valueOf(10L), codigoGerado);
		verify(caixas, times(1)).save(caixa);
		verify(lancamentos, times(1)).lancamento(any(CaixaLancamento.class));
	}

	@Test
	public void testCadastro_CofreComDescricaoVazia_UsaDescricaoPadrao() {
		Caixa caixa = new Caixa();
		caixa.setCodigo(11L);
		caixa.setTipo(CaixaTipo.COFRE);
		caixa.setDescricao("");
		caixa.setValor_abertura(50.0);

		caixaService.cadastro(caixa);

		assertEquals("Cofre", caixa.getDescricao());
		verify(caixas, times(1)).save(caixa);
		verify(lancamentos, times(1)).lancamento(any(CaixaLancamento.class));
	}

	@Test
	public void testCadastro_BancoComDescricaoVazia_LimpaAgenciaEConta() {
		Caixa caixa = new Caixa();
		caixa.setCodigo(12L);
		caixa.setTipo(CaixaTipo.BANCO);
		caixa.setDescricao("");
		caixa.setAgencia("1234-X");
		caixa.setConta("98765-0");
		caixa.setValor_abertura(200.0);

		caixaService.cadastro(caixa);

		assertEquals("Banco", caixa.getDescricao());
		assertEquals("1234", caixa.getAgencia());
		assertEquals("987650", caixa.getConta());
		verify(caixas, times(1)).save(caixa);
		verify(lancamentos, times(1)).lancamento(any(CaixaLancamento.class));
	}

	@Test
	public void testCadastro_CaixaComValorAberturaNulo_DeveSetarZero() {
		Caixa caixa = new Caixa();
		caixa.setTipo(CaixaTipo.CAIXA);
		caixa.setDescricao("Meu Caixa");
		caixa.setValor_abertura(null);

		when(caixas.caixaAberto()).thenReturn(Optional.empty());

		caixaService.cadastro(caixa);

		assertEquals(Double.valueOf(0.0), caixa.getValor_abertura());
		assertEquals(Double.valueOf(0.0), caixa.getValor_total());
		verify(lancamentos, never()).lancamento(any(CaixaLancamento.class));
	}

	@Test
	public void testCadastro_CaixaJaAberto_LancaExcecao() {
		Caixa caixa = new Caixa();
		caixa.setTipo(CaixaTipo.CAIXA);

		when(caixas.caixaAberto()).thenReturn(Optional.of(new Caixa()));

		try {
			caixaService.cadastro(caixa);
			fail("Deveria ter lançado exceção bloqueando abertura simultânea");
		} catch (RuntimeException e) {
			assertEquals("Existe caixa de dias anteriores em aberto, favor verifique", e.getMessage());
		}

		verify(caixas, never()).save(any());
	}

	@Test
	public void testCadastro_ValorNegativo_LancaExcecao() {
		Caixa caixa = new Caixa();
		caixa.setTipo(CaixaTipo.COFRE);
		caixa.setValor_abertura(-50.0);

		try {
			caixaService.cadastro(caixa);
			fail("Deveria ter lançado exceção para valor negativo");
		} catch (RuntimeException e) {
			assertEquals("Valor informado é inválido", e.getMessage());
		}

		verify(caixas, never()).save(any());
	}

	@Test
	public void testCadastro_ErroAoSalvar_LancaExcecao() {
		Caixa caixa = new Caixa();
		caixa.setTipo(CaixaTipo.COFRE);
		caixa.setDescricao("Cofre Principal");
		caixa.setValor_abertura(0.0);

		doThrow(new RuntimeException("Erro BD")).when(caixas).save(caixa);

		try {
			caixaService.cadastro(caixa);
			fail("Deveria ter lançado exceção ao falhar no save");
		} catch (RuntimeException e) {
			assertEquals("Erro no processo de abertura, chame o suporte técnico", e.getMessage());
		}
	}

	@Test
	public void testCadastro_ErroAoGerarLancamento_LancaExcecao() {
		Caixa caixa = new Caixa();
		caixa.setTipo(CaixaTipo.CAIXA);
		caixa.setDescricao("Caixa Teste");
		caixa.setValor_abertura(100.0);

		when(caixas.caixaAberto()).thenReturn(Optional.empty());
		doThrow(new RuntimeException("Erro lançamento")).when(lancamentos).lancamento(any(CaixaLancamento.class));

		try {
			caixaService.cadastro(caixa);
			fail("Deveria ter lançado exceção ao falhar no lançamento");
		} catch (RuntimeException e) {
			assertEquals("Erro no processo, chame o suporte", e.getMessage());
		}
	}

	@Test
	public void testFechaCaixa_ComSucesso() {
		Caixa caixaAberto = new Caixa();
		caixaAberto.setCodigo(1L);
		caixaAberto.setValor_total(500.0);

		when(caixas.findById(1L)).thenReturn(Optional.of(caixaAberto));

		String resposta = caixaService.fechaCaixa(1L, "senha123");

		assertEquals("Caixa fechado com sucesso", resposta);
		assertNotNull(caixaAberto.getData_fechamento());
		assertEquals(Double.valueOf(500.0), caixaAberto.getValor_fechamento());
		verify(caixas, times(1)).save(caixaAberto);
	}

	@Test
	public void testFechaCaixa_ValorTotalNulo_AssumeZero() {
		Caixa caixaAberto = new Caixa();
		caixaAberto.setCodigo(2L);
		caixaAberto.setValor_total(null);

		when(caixas.findById(2L)).thenReturn(Optional.of(caixaAberto));

		String resposta = caixaService.fechaCaixa(2L, "senha123");

		assertEquals("Caixa fechado com sucesso", resposta);
		assertEquals(Double.valueOf(0.0), caixaAberto.getValor_fechamento());
		verify(caixas, times(1)).save(caixaAberto);
	}

	@Test
	public void testFechaCaixa_SenhaVazia_RetornaMensagem() {
		String resposta = caixaService.fechaCaixa(1L, "");
		assertEquals("Favor, informe a senha", resposta);
	}

	@Test
	public void testFechaCaixa_SenhaIncorreta_RetornaMensagem() {
		String resposta = caixaService.fechaCaixa(1L, "senhaErrada");
		assertEquals("Senha incorreta, favor verifique", resposta);
	}

	@Test
	public void testFechaCaixa_CaixaJaFechado_LancaExcecao() {
		Caixa caixaFechado = new Caixa();
		caixaFechado.setCodigo(1L);
		caixaFechado.setData_fechamento(new Timestamp(System.currentTimeMillis()));

		when(caixas.findById(1L)).thenReturn(Optional.of(caixaFechado));

		try {
			caixaService.fechaCaixa(1L, "senha123");
			fail("Deveria ter lançado exceção pois caixa já estava fechado");
		} catch (RuntimeException e) {
			assertEquals("Caixa já esta fechado", e.getMessage());
		}
	}

	@Test
	public void testFechaCaixa_CaixaInexistente_LancaExcecao() {
		when(caixas.findById(999L)).thenReturn(Optional.empty());

		try {
			caixaService.fechaCaixa(999L, "senha123");
			fail("Deveria ter lançado exceção para caixa inexistente");
		} catch (RuntimeException e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	public void testFechaCaixa_ErroAoSalvar_LancaExcecao() {
		Caixa caixaAberto = new Caixa();
		caixaAberto.setCodigo(3L);
		caixaAberto.setValor_total(150.0);

		when(caixas.findById(3L)).thenReturn(Optional.of(caixaAberto));
		doThrow(new RuntimeException("Erro ao salvar")).when(caixas).save(caixaAberto);

		try {
			caixaService.fechaCaixa(3L, "senha123");
			fail("Deveria ter lançado exceção ao falhar ao fechar caixa");
		} catch (RuntimeException e) {
			assertEquals("Ocorreu um erro ao fechar o caixa, chame o suporte", e.getMessage());
		}
	}

	@Test
	public void testConsultasEFiltros() {
		Caixa caixa = new Caixa();
		caixa.setCodigo(5L);
		List<Caixa> lista = Collections.singletonList(caixa);

		when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
		assertTrue(caixaService.caixaIsAberto());
		assertTrue(caixaService.caixaAberto().isPresent());

		when(caixas.findByCodigoOrdenado()).thenReturn(lista);
		assertEquals(1, caixaService.listaTodos().size());

		when(caixas.caixasAbertos()).thenReturn(lista);
		assertEquals(1, caixaService.caixasAbertos().size());

		when(caixas.findById(5L)).thenReturn(Optional.of(caixa));
		assertSame(caixa, caixaService.busca(5L).orElse(null));

		when(caixas.findByCaixaAbertoUsuario(1L)).thenReturn(caixa);
		assertTrue(caixaService.buscaCaixaUsuario("usuarioTeste").isPresent());

		when(caixas.buscaBancos(CaixaTipo.BANCO)).thenReturn(lista);
		assertEquals(1, caixaService.listaBancos().size());

		when(caixas.buscaCaixaTipo(CaixaTipo.COFRE)).thenReturn(Arrays.asList(caixa));
		assertEquals(1, caixaService.listaCaixasAbertosTipo(CaixaTipo.COFRE).size());
	}

	@Test
	public void testListarCaixas_ComEValorSemData() {
		CaixaFilter filterComData = new CaixaFilter();
		filterComData.setData_cadastro("2026/11/22");
		when(caixas.buscaCaixasPorDataAbertura(Date.valueOf("2026-11-22"))).thenReturn(Collections.singletonList(new Caixa()));
		assertEquals(1, caixaService.listarCaixas(filterComData).size());

		CaixaFilter filterVazio = new CaixaFilter();
		filterVazio.setData_cadastro("");
		when(caixas.listaCaixasAbertos()).thenReturn(Collections.emptyList());
		assertFalse(caixaService.listarCaixas(filterVazio).iterator().hasNext());

		CaixaFilter filterNulo = new CaixaFilter();
		filterNulo.setData_cadastro(null);
		assertFalse(caixaService.listarCaixas(filterNulo).iterator().hasNext());
	}

	@Test
	public void testListaBancosAbertosTipoFilterBanco_ComESemData() {
		BancoFilter filterComData = new BancoFilter();
		filterComData.setData_cadastro("2026/11/22");
		when(caixas.buscaCaixaTipoData(CaixaTipo.BANCO, Date.valueOf("2026-11-22")))
				.thenReturn(Collections.singletonList(new Caixa()));
		assertEquals(1, caixaService.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filterComData).size());

		BancoFilter filterSemData = new BancoFilter();
		filterSemData.setData_cadastro("");
		when(caixas.buscaCaixaTipo(CaixaTipo.BANCO)).thenReturn(Collections.emptyList());
		assertEquals(0, caixaService.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filterSemData).size());
	}
}