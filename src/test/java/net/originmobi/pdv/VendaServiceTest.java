package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.VendaSituacao;
import net.originmobi.pdv.filter.VendaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.PagamentoTipo;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.model.Receber;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.TituloTipo;
import net.originmobi.pdv.model.Venda;
import net.originmobi.pdv.repository.VendaRepository;
import net.originmobi.pdv.service.CaixaLancamentoService;
import net.originmobi.pdv.service.CaixaService;
import net.originmobi.pdv.service.PagamentoTipoService;
import net.originmobi.pdv.service.ParcelaService;
import net.originmobi.pdv.service.ProdutoService;
import net.originmobi.pdv.service.ReceberService;
import net.originmobi.pdv.service.UsuarioService;
import net.originmobi.pdv.service.VendaProdutoService;
import net.originmobi.pdv.service.VendaService;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;

@RunWith(MockitoJUnitRunner.class)
public class VendaServiceTest {

    @InjectMocks
    private VendaService vendaService;

    @Mock private VendaRepository vendas;
    @Mock private UsuarioService usuarios;
    @Mock private VendaProdutoService vendaProdutos;
    @Mock private PagamentoTipoService formaPagamentos;
    @Mock private CaixaService caixas;
    @Mock private ReceberService receberServ;
    @Mock private ParcelaService parcelas;
    @Mock private CaixaLancamentoService lancamentos;
    @Mock private TituloService tituloService;
    @Mock private CartaoLancamentoService cartaoLancamento;
    @Mock private ProdutoService produtos;

    private Venda vendaMock;

    @Before
    public void setUp() {
        vendaMock = new Venda();
        vendaMock.setCodigo(1L);
        vendaMock.setSituacao(VendaSituacao.ABERTA);
    }

    @Test
    public void testAbreVenda_VendaExistente() {
        vendaMock.setPessoa(new Pessoa());
        vendaMock.setObservacao("Obs");
        
        Long codigo = vendaService.abreVenda(vendaMock);
        
        verify(vendas, times(1)).updateDadosVenda(any(), anyString(), anyLong());
        assertEquals(Long.valueOf(1L), codigo);
    }

    @Test
    public void testBusca_ComCodigo() {
        VendaFilter filter = new VendaFilter();
        filter.setCodigo(1L);
        
        vendaService.busca(filter, "ABERTA", Pageable.unpaged());
        
        verify(vendas).findByCodigoIn(eq(filter.getCodigo()), any());
    }

    @Test
    public void testBusca_SemCodigo() {
        VendaFilter filter = new VendaFilter();
        
        vendaService.busca(filter, "FECHADA", Pageable.unpaged());
        
        verify(vendas).findBySituacaoEquals(eq(VendaSituacao.FECHADA), any());
    }

    @Test
    public void testAddProduto_VendaAberta() {
        when(vendas.verificaSituacao(1L)).thenReturn(VendaSituacao.ABERTA.toString());
        
        String result = vendaService.addProduto(1L, 2L, 10.0);
        
        verify(vendaProdutos).salvar(any());
        assertEquals("ok", result);
    }

    @Test
    public void testAddProduto_VendaFechada() {
        when(vendas.verificaSituacao(1L)).thenReturn(VendaSituacao.FECHADA.toString());
        
        String result = vendaService.addProduto(1L, 2L, 10.0);
        assertEquals("Venda fechada", result);
    }

    @Test
    public void testRemoveProduto_VendaAberta() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        
        String result = vendaService.removeProduto(1L, 1L);
        
        verify(vendaProdutos).removeProduto(1L);
        assertEquals("ok", result);
    }

    @Test
    public void testRemoveProduto_VendaFechada() {
        vendaMock.setSituacao(VendaSituacao.FECHADA);
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        
        String result = vendaService.removeProduto(1L, 1L);
        assertEquals("Venda fechada", result);
    }

    @Test
    public void testLista() {
        when(vendas.findAll()).thenReturn(Collections.singletonList(vendaMock));
        assertEquals(1, vendaService.lista().size());
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_VendaFechada_LancaExcecao() {
        vendaMock.setSituacao(VendaSituacao.FECHADA);
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        
        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0, new String[]{}, new String[]{});
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_ValorZero_LancaExcecao() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        
        vendaService.fechaVenda(1L, 1L, 0.0, 0.0, 0.0, new String[]{}, new String[]{});
    }

    @Test
    public void testFechaVenda_AvistaCartaoCredito() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        
        PagamentoTipo pagTipo = new PagamentoTipo();
        pagTipo.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagTipo);
        
        TituloTipo tipoTit = new TituloTipo();
        tipoTit.setSigla(net.originmobi.pdv.enumerado.TituloTipo.CARTCRED.toString());
        Titulo titulo = new Titulo();
        titulo.setTipo(tipoTit);
        when(tituloService.busca(2L)).thenReturn(Optional.of(titulo));
        
        String[] vlParcelas = {"100.0"};
        String[] titulos = {"2"};
        
        String result = vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0, vlParcelas, titulos);
        
        verify(cartaoLancamento).lancamento(anyDouble(), any());
        verify(vendas).fechaVenda(anyLong(), any(), anyDouble(), anyDouble(), anyDouble(), any(Timestamp.class), any());
        verify(produtos).movimentaEstoque(1L, EntradaSaida.SAIDA);
        assertEquals("Venda finalizada com sucesso", result);
    }
    
    @Test
    public void testQtdAbertos() {
        when(vendas.qtdVendasEmAberto()).thenReturn(5);
        assertEquals(5, vendaService.qtdAbertos());
    }
}