package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
	}

	@Test
	public void testListaProdutosAjuste() {
		AjusteProduto item = new AjusteProduto();
		when(ajusteprodutos.findByAjusteCodigoEquals(1L)).thenReturn(Collections.singletonList(item));

		List<AjusteProduto> resultado = ajusteProdutoService.listaProdutosAjuste(1L);

		assertEquals(1, resultado.size());
		verify(ajusteprodutos).findByAjusteCodigoEquals(1L);
	}

	@Test
	public void testBuscaProdAjust() {
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(1);

		int resultado = ajusteProdutoService.buscaProdAjust(1L, 2L);

		assertEquals(1, resultado);
	}

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

	@Test(expected = RuntimeException.class)
	public void testAddProduto_AjusteJaProcessado_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteProcessado));

		ajusteProdutoService.addProduto(1L, 2L, 5);
	}

	@Test(expected = RuntimeException.class)
	public void testAddProduto_ProdutoJaExisteNoAjuste_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(1);

		ajusteProdutoService.addProduto(1L, 2L, 5);
	}

	@Test(expected = RuntimeException.class)
	public void testAddProduto_QuantidadeZero_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);

		ajusteProdutoService.addProduto(1L, 2L, 0);
	}

	@Test(expected = RuntimeException.class)
	public void testAddProduto_ErroAoInserir_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		when(produtos.busca(2L)).thenReturn(produto);
		when(ajusteprodutos.buscaProdAjuste(1L, 2L)).thenReturn(0);
		doThrow(new RuntimeException("erro de banco")).when(ajusteprodutos)
				.insereProduto(anyLong(), anyLong(), anyInt(), anyInt(), anyInt());

		ajusteProdutoService.addProduto(1L, 2L, 5);
	}

	@Test
	public void testRemoveProduto_ComSucesso() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		doNothing().when(ajusteprodutos).removeProduto(1L, 3L);

		String resultado = ajusteProdutoService.removeProduto(1L, 3L);

		verify(ajusteprodutos).removeProduto(1L, 3L);
		assertEquals("Produto removido com sucesso", resultado);
	}

	@Test(expected = RuntimeException.class)
	public void testRemoveProduto_AjusteJaProcessado_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteProcessado));

		ajusteProdutoService.removeProduto(1L, 3L);
	}

	@Test(expected = RuntimeException.class)
	public void testRemoveProduto_ErroAoRemover_LancaExcecao() {
		when(ajustes.busca(1L)).thenReturn(Optional.of(ajusteAberto));
		doThrow(new RuntimeException("erro de banco")).when(ajusteprodutos).removeProduto(1L, 3L);

		ajusteProdutoService.removeProduto(1L, 3L);
	}

}
