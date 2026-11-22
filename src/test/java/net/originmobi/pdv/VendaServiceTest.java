package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.VendaSituacao;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.VendaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.PagamentoTipo;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.model.Receber;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.TituloTipo;
import net.originmobi.pdv.model.Usuario;
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

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testAbreVenda_NovaVenda() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("usuario", null));

        Usuario usuario = new Usuario();
        when(usuarios.buscaUsuario("usuario")).thenReturn(usuario);
        when(vendas.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda vendaSalva = invocation.getArgument(0);
            vendaSalva.setCodigo(2L);
            return vendaSalva;
        });

        Venda novaVenda = new Venda();
        Long codigo = vendaService.abreVenda(novaVenda);

        ArgumentCaptor<Venda> vendaSalva = ArgumentCaptor.forClass(Venda.class);
        verify(vendas).save(vendaSalva.capture());
        verify(usuarios).buscaUsuario("usuario");

        assertEquals(2L, codigo.longValue());
        assertEquals(2L, vendaSalva.getValue().getCodigo().longValue());
        assertEquals(VendaSituacao.ABERTA, vendaSalva.getValue().getSituacao());
        assertEquals(usuario, vendaSalva.getValue().getUsuario());
        assertEquals(0.00, vendaSalva.getValue().getValor_produtos(), 0.0);
        assertEquals(2L, novaVenda.getCodigo().longValue());
    }

    @Test
    public void testAbreVenda_VendaExistente() {
        vendaMock.setPessoa(new Pessoa());
        vendaMock.setObservacao("Obs");
        
        Long codigo = vendaService.abreVenda(vendaMock);
        
        verify(vendas).updateDadosVenda(eq(vendaMock.getPessoa()), eq("Obs"), eq(1L));
        assertEquals(1L, codigo.longValue());
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
        
        ArgumentCaptor<net.originmobi.pdv.model.VendaProduto> produto =
                ArgumentCaptor.forClass(net.originmobi.pdv.model.VendaProduto.class);
        verify(vendaProdutos).salvar(produto.capture());
        assertEquals(1L, produto.getValue().getVenda().longValue());
        assertEquals(2L, produto.getValue().getProduto().longValue());
        assertEquals(10.0, produto.getValue().getValor_balanca(), 0.0);
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
        
        verify(vendas).findByCodigoEquals(1L);
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
        
        ArgumentCaptor<Double> valor = ArgumentCaptor.forClass(Double.class);
        verify(cartaoLancamento).lancamento(valor.capture(), eq(Optional.of(titulo)));
        verify(vendas).fechaVenda(eq(1L), eq(VendaSituacao.FECHADA), eq(100.0),
                eq(0.0), eq(0.0), any(Timestamp.class), eq(pagTipo));
        verify(produtos).movimentaEstoque(eq(1L), eq(EntradaSaida.SAIDA));
        verify(parcelas, never()).gerarParcela(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(),                 anyDouble(), any(Receber.class), anyInt(), anyInt(),
                any(Timestamp.class), any(java.sql.Date.class));
        assertEquals(100.0, valor.getValue(), 0.0);
        assertEquals("Venda finalizada com sucesso", result);
    }

    @Test
    public void testFechaVenda_AvistaCartaoDebito() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);

        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);

        TituloTipo tipo = new TituloTipo();
        tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.CARTDEB.toString());
        Titulo titulo = new Titulo();
        titulo.setTipo(tipo);
        when(tituloService.busca(2L)).thenReturn(Optional.of(titulo));

        String result = vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        verify(cartaoLancamento).lancamento(eq(100.0), eq(Optional.of(titulo)));
        verify(lancamentos, never()).lancamento(any());
        verify(parcelas, never()).gerarParcela(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(),                 anyDouble(), any(Receber.class), anyInt(), anyInt(),
                any(Timestamp.class), any(java.sql.Date.class));
        verify(produtos).movimentaEstoque(1L, EntradaSaida.SAIDA);
        assertEquals("Venda finalizada com sucesso", result);
    }

    @Test
    public void testFechaVenda_AvistaDinheiro_CaixaAberto() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("usuario", null));
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);

        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);

        TituloTipo tipo = new TituloTipo();
        tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
        Titulo titulo = new Titulo();
        titulo.setTipo(tipo);
        when(tituloService.busca(2L)).thenReturn(Optional.of(titulo));

        Usuario usuario = new Usuario();
        Caixa caixa = new Caixa();
        when(caixas.caixaIsAberto()).thenReturn(true);
        when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
        when(usuarios.buscaUsuario("usuario")).thenReturn(usuario);

        String result = vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        ArgumentCaptor<net.originmobi.pdv.model.CaixaLancamento> lancamento =
                ArgumentCaptor.forClass(net.originmobi.pdv.model.CaixaLancamento.class);
        verify(lancamentos).lancamento(lancamento.capture());
        assertEquals(100.0, lancamento.getValue().getValor(), 0.0);
        assertEquals(TipoLancamento.RECEBIMENTO, lancamento.getValue().getTipo());
        assertEquals(EstiloLancamento.ENTRADA, lancamento.getValue().getEstilo());
        assertEquals(caixa, lancamento.getValue().getCaixa().get());
        assertEquals(usuario, lancamento.getValue().getUsuario());
        verify(cartaoLancamento, never()).lancamento(anyDouble(), any());
        verify(parcelas, never()).gerarParcela(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(),                 anyDouble(), any(Receber.class), anyInt(), anyInt(),
                any(Timestamp.class), any(java.sql.Date.class));
        verify(produtos).movimentaEstoque(1L, EntradaSaida.SAIDA);
        assertEquals("Venda finalizada com sucesso", result);
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_AvistaDinheiro_CaixaFechado() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("usuario", null));
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);

        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        TituloTipo tipo = new TituloTipo();
        tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
        Titulo titulo = new Titulo();
        titulo.setTipo(tipo);
        when(tituloService.busca(2L)).thenReturn(Optional.of(titulo));
        when(caixas.caixaIsAberto()).thenReturn(false);

        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        verify(lancamentos, never()).lancamento(any());
        verify(vendas, never()).fechaVenda(anyLong(), any(), anyDouble(), anyDouble(),
                anyDouble(), any(Timestamp.class), any());
        verify(produtos, never()).movimentaEstoque(anyLong(), any(EntradaSaida.class));
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_AvistaDinheiro_ParcelaVazia() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("usuario", null));
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        TituloTipo tipo = new TituloTipo();
        tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
        Titulo titulo = new Titulo();
        titulo.setTipo(tipo);
        when(tituloService.busca(2L)).thenReturn(Optional.of(titulo));
        when(caixas.caixaIsAberto()).thenReturn(true);

        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {""}, new String[] {"2"});

        verify(lancamentos, never()).lancamento(any());
        verify(produtos, never()).movimentaEstoque(anyLong(), any(EntradaSaida.class));
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_AvistaDinheiro_ParcelasDivergentes() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("usuario", null));
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        TituloTipo tipo = new TituloTipo();
        tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
        Titulo titulo = new Titulo();
        titulo.setTipo(tipo);
        when(tituloService.busca(2L)).thenReturn(Optional.of(titulo));
        when(caixas.caixaIsAberto()).thenReturn(true);

        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"90.0"}, new String[] {"2"});

        verify(lancamentos, never()).lancamento(any());
        verify(produtos, never()).movimentaEstoque(anyLong(), any(EntradaSaida.class));
    }

    @Test
    public void testFechaVenda_Aprazo_GeraParcela() {
        vendaMock.setPessoa(new Pessoa());
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);

        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("30");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        when(tituloService.busca(2L)).thenReturn(Optional.of(new Titulo()));

        String result = vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        verify(parcelas).gerarParcela(eq(100.0), eq(0.0), eq(0.0), eq(0.0),
                eq(100.0), any(Receber.class), eq(0), eq(1),
                any(Timestamp.class), any(java.sql.Date.class));
        verify(cartaoLancamento, never()).lancamento(anyDouble(), any());
        verify(lancamentos, never()).lancamento(any());
        verify(produtos).movimentaEstoque(1L, EntradaSaida.SAIDA);
        assertEquals("Venda finalizada com sucesso", result);
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_Aprazo_SemCliente() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("30");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        when(tituloService.busca(2L)).thenReturn(Optional.of(new Titulo()));

        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        verify(parcelas, never()).gerarParcela(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(),                 anyDouble(), any(Receber.class), anyInt(), anyInt(),
                any(Timestamp.class), any(java.sql.Date.class));
        verify(produtos, never()).movimentaEstoque(anyLong(), any(EntradaSaida.class));
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_ErroAoCadastrarReceber_NaoFechaVenda() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        doThrow(new IllegalStateException("falha"))
                .when(receberServ).cadastrar(any(Receber.class));

        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        verify(vendas, never()).fechaVenda(anyLong(), any(), anyDouble(), anyDouble(),
                anyDouble(), any(Timestamp.class), any());
        verify(produtos, never()).movimentaEstoque(anyLong(), any(EntradaSaida.class));
    }

    @Test(expected = RuntimeException.class)
    public void testFechaVenda_TituloInexistente_NaoFechaVenda() {
        when(vendas.findByCodigoEquals(1L)).thenReturn(vendaMock);
        PagamentoTipo pagamento = new PagamentoTipo();
        pagamento.setFormaPagamento("00");
        when(formaPagamentos.busca(1L)).thenReturn(pagamento);
        when(tituloService.busca(2L)).thenReturn(Optional.empty());

        vendaService.fechaVenda(1L, 1L, 100.0, 0.0, 0.0,
                new String[] {"100.0"}, new String[] {"2"});

        verify(vendas, never()).fechaVenda(anyLong(), any(), anyDouble(), anyDouble(),
                anyDouble(), any(Timestamp.class), any());
        verify(produtos, never()).movimentaEstoque(anyLong(), any(EntradaSaida.class));
    }
    
    @Test
    public void testQtdAbertos() {
        when(vendas.qtdVendasEmAberto()).thenReturn(5);
        assertEquals(5, vendaService.qtdAbertos());
    }
}