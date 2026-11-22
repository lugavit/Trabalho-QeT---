package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.originmobi.pdv.enumerado.ajuste.AjusteStatus;
import net.originmobi.pdv.model.Ajuste;
import net.originmobi.pdv.model.AjusteProduto;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.model.ProdutoEstoque;
import net.originmobi.pdv.repository.AjusteProdutoRepository;
import net.originmobi.pdv.service.AjusteProdutoService;
import net.originmobi.pdv.service.AjusteService;
import net.originmobi.pdv.service.ProdutoService;

@RunWith(MockitoJUnitRunner.class)
public class AjusteProdutoServiceTest {

	@InjectMocks
	private AjusteProdutoService ajusteProdutoService;

	@Mock
	private AjusteProdutoRepository ajusteprodutos;

	@Mock
	private ProdutoService produtos;

	@Mock
	private AjusteService ajustes;

	private Ajuste ajusteAberto;
	private Ajuste ajusteProcessado;
	private Produto produto;

	private PrintStream saidaOriginal;
	private ByteArrayOutputStream saidaCapturada;

	@Before
	public void setUp() {
		ajusteAberto = new Ajuste();
		ajusteAberto.setCodigo(1L);
		ajusteAberto.setStatus(AjusteStatus.APROCESSAR);

		ajusteProcessado = new Ajuste();
		ajusteProcessado.setCodigo(1L);
		ajusteProcessado.setStatus(AjusteStatus.PROCESSADO);

		ProdutoEstoque estoque = new ProdutoEstoque();
		estoque.setQtd(10);

		produto = new Produto();
		produto.setEstoque(estoque);

		// captura o System.out para verificar o log de erro feito pela classe
		saidaOriginal = System.out;
		saidaCapturada = new ByteArrayOutputStream();
		System.setOut(new PrintStream(saidaCapturada));
	}

	@After
	public void tearDown() {
		System.setOut(saidaOriginal);
	}

	/**
	 * Executa a ação e devolve a RuntimeException lançada. Falha o teste se nada
	 * for lançado. (Em vez de assertThrows, que só existe a partir do JUnit 4.13.)
	 */
	private RuntimeException capturaErro(Runnable acao) {
		try {
			acao.run();
		} catch (RuntimeException e) {
			return e;
		}
		fail("Era esperada uma RuntimeException, mas nada foi lançado");
		return null;
	}

	private void assertErroDeNegocio(String mensagemEsperada, RuntimeException e) {
		// getClass() exato: garante que é o erro de negócio e não uma subclasse
		// acidental (ex.: NoSuchElementException)
		assertEquals(RuntimeException.class, e.getClass());
		assertEquals(mensagemEsperada, e.getMessage());
	}

	// ------------------------------------------------------------ consultas

	@Test
	public void testListaProdutosAjuste() {
		AjusteProduto item = new AjusteProduto();
		when(ajusteprodutos.findByAjusteCodigoEquals(1L)).thenReturn(Collections.singletonList(item));

		List<AjusteProduto> resultado = ajusteProdutoService.listaProdutosAjuste(1L);

		assertEquals(1, resultado.size());
		assertEquals(item, resultado.get(0));
		verify(ajusteprodutos).findByAjusteCodigoEquals(1L);
	}

	@Test
	public void testBuscaProdAjust() {
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(1);

		int resultado = ajusteProdutoService.buscaProdAjust(1L, 2L);

		assertEquals(1, resultado);
	}

	// ----------------------------------------------------------- addProduto

	@Test
	public void testAddProduto_ComSucesso() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);
		doNothing().when(ajusteprodutos).insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());

		String resultado = ajusteProdutoService.addProduto(1L, 2L, 5);

		verify(ajusteprodutos).insereProduto(1L, 2L, 10, 5, 15);
		assertEquals("Ajuste processado com sucesso", resultado);
	}

	@Test
	public void testAddProduto_QuantidadeNegativa_SubtraiDoEstoque() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);

		ajusteProdutoService.addProduto(1L, 2L, -3);

		verify(ajusteprodutos).insereProduto(1L, 2L, 10, -3, 7);
	}

	@Test
	public void testAddProduto_AjusteJaProcessado_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteProcessado));

		RuntimeException e = capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 5));

		assertErroDeNegocio("Ajuste já esta processado", e);
		verify(ajusteprodutos, never()).insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());
	}

	@Test
	public void testAddProduto_ProdutoJaExisteNoAjuste_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(1);

		RuntimeException e = capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 5));

		assertErroDeNegocio("Este produto já existe neste ajuste", e);
		verify(ajusteprodutos, never()).insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());
	}

	@Test
	public void testAddProduto_QuantidadeZero_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);

		RuntimeException e = capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 0));

		assertErroDeNegocio("Quantidade inválido", e);
		verify(ajusteprodutos, never()).insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());
	}

	/**
	 * A segunda verificação de PROCESSADO (linha 52 do serviço) só é alcançável se
	 * o status mudar entre as duas leituras. Um mock de Ajuste que devolve status
	 * diferentes em chamadas consecutivas simula isso e cobre o trecho.
	 */
	@Test
	public void testAddProduto_StatusMudaEntreVerificacoes_LancaExcecao() {
		Ajuste ajusteInstavel = mock(Ajuste.class);
		when(ajusteInstavel.getStatus()).thenReturn(AjusteStatus.APROCESSAR, AjusteStatus.PROCESSADO);
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteInstavel));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);

		RuntimeException e = capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 5));

		assertErroDeNegocio("Ajuste já processado", e);
		verify(ajusteprodutos, never()).insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());
	}

	@Test
	public void testAddProduto_AjusteInexistente_LancaNoSuchElementException() {
		when(ajustes.busca(1L)).thenReturn(Optional.empty());

		// documenta o comportamento atual: Optional.get() sem checagem de presença
		RuntimeException e = capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 5));

		assertEquals(NoSuchElementException.class, e.getClass());
	}

	@Test
	public void testAddProduto_ErroAoInserir_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);
		doThrow(new RuntimeException("falha simulada no banco")).when(ajusteprodutos).insereProduto(anyLong(),
				anyLong(), anyInt(), anyInt(), anyInt());

		RuntimeException e = capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 5));

		assertErroDeNegocio("Erro ao tentar inserir produto no ajuste, chame o suporte", e);
	}

	@Test
	public void testAddProduto_ErroAoInserir_RegistraErroNoConsole() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);
		doThrow(new RuntimeException("falha simulada no banco")).when(ajusteprodutos).insereProduto(anyLong(),
				anyLong(), anyInt(), anyInt(), anyInt());

		capturaErro(() -> ajusteProdutoService.addProduto(1L, 2L, 5));

		assertTrue(saidaCapturada.toString().contains("falha simulada no banco"));
	}

	// -------------------------------------------------------- removeProduto

	@Test
	public void testRemoveProduto_ComSucesso() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		doNothing().when(ajusteprodutos).removeProduto(1L, 3L);

		String resultado = ajusteProdutoService.removeProduto(1L, 3L);

		verify(ajusteprodutos).removeProduto(1L, 3L);
		assertEquals("Produto removido com sucesso", resultado);
	}

	@Test
	public void testRemoveProduto_AjusteJaProcessado_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteProcessado));

		RuntimeException e = capturaErro(() -> ajusteProdutoService.removeProduto(1L, 3L));

		assertErroDeNegocio("Ajuste já esta processado", e);
		verify(ajusteprodutos, never()).removeProduto(anyLong(), anyLong());
	}

	@Test
	public void testRemoveProduto_AjusteInexistente_LancaNoSuchElementException() {
		when(ajustes.busca(1L)).thenReturn(Optional.empty());

		RuntimeException e = capturaErro(() -> ajusteProdutoService.removeProduto(1L, 3L));

		assertEquals(NoSuchElementException.class, e.getClass());
	}

	@Test
	public void testRemoveProduto_ErroAoRemover_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		doThrow(new RuntimeException("falha simulada no banco")).when(ajusteprodutos).removeProduto(1L, 3L);

		RuntimeException e = capturaErro(() -> ajusteProdutoService.removeProduto(1L, 3L));

		assertErroDeNegocio("Erro ao tentar remover produto do ajuste, chame o suporte", e);
	}

	@Test
	public void testRemoveProduto_ErroAoRemover_RegistraErroNoConsole() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		doThrow(new RuntimeException("falha simulada no banco")).when(ajusteprodutos).removeProduto(1L, 3L);

		capturaErro(() -> ajusteProdutoService.removeProduto(1L, 3L));

		assertTrue(saidaCapturada.toString().contains("falha simulada no banco"));
	}

}
