// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package net.originmobi.pdv;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Optional;
import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.VendaSituacao;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.VendaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.PagamentoTipo;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.model.Receber;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.TituloTipo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.model.Venda;
import net.originmobi.pdv.model.VendaProduto;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class VendaServiceTest {
   @InjectMocks
   private VendaService vendaService;
   @Mock
   private VendaRepository vendas;
   @Mock
   private UsuarioService usuarios;
   @Mock
   private VendaProdutoService vendaProdutos;
   @Mock
   private PagamentoTipoService formaPagamentos;
   @Mock
   private CaixaService caixas;
   @Mock
   private ReceberService receberServ;
   @Mock
   private ParcelaService parcelas;
   @Mock
   private CaixaLancamentoService lancamentos;
   @Mock
   private TituloService tituloService;
   @Mock
   private CartaoLancamentoService cartaoLancamento;
   @Mock
   private ProdutoService produtos;
   private Venda vendaMock;

   public VendaServiceTest() {
   }

   @Before
   public void setUp() {
      this.vendaMock = new Venda();
      this.vendaMock.setCodigo(1L);
      this.vendaMock.setSituacao(VendaSituacao.ABERTA);
   }

   @After
   public void tearDown() {
      SecurityContextHolder.clearContext();
   }

   @Test
   public void testAbreVenda_NovaVenda() {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("usuario", (Object)null));
      Usuario usuario = new Usuario();
      Mockito.when(this.usuarios.buscaUsuario("usuario")).thenReturn(usuario);
      Mockito.when((Venda)this.vendas.save((Venda)ArgumentMatchers.any(Venda.class))).thenAnswer((invocation) -> {
         Venda vendaSalva = (Venda)invocation.getArgument(0);
         vendaSalva.setCodigo(2L);
         return vendaSalva;
      });
      Venda novaVenda = new Venda();
      Long codigo = this.vendaService.abreVenda(novaVenda);
      ArgumentCaptor<Venda> vendaSalva = ArgumentCaptor.forClass(Venda.class);
      ((VendaRepository)Mockito.verify(this.vendas)).save((Venda)vendaSalva.capture());
      ((UsuarioService)Mockito.verify(this.usuarios)).buscaUsuario("usuario");
      Assert.assertEquals(2L, codigo.longValue());
      Assert.assertEquals(2L, ((Venda)vendaSalva.getValue()).getCodigo().longValue());
      Assert.assertEquals(VendaSituacao.ABERTA, ((Venda)vendaSalva.getValue()).getSituacao());
      Assert.assertEquals(usuario, ((Venda)vendaSalva.getValue()).getUsuario());
      Assert.assertEquals(0.0, ((Venda)vendaSalva.getValue()).getValor_produtos(), 0.0);
      Assert.assertEquals(2L, novaVenda.getCodigo().longValue());
   }

   @Test
   public void testAbreVenda_VendaExistente() {
      this.vendaMock.setPessoa(new Pessoa());
      this.vendaMock.setObservacao("Obs");
      Long codigo = this.vendaService.abreVenda(this.vendaMock);
      ((VendaRepository)Mockito.verify(this.vendas)).updateDadosVenda((Pessoa)ArgumentMatchers.eq(this.vendaMock.getPessoa()), (String)ArgumentMatchers.eq("Obs"), ArgumentMatchers.eq(1L));
      Assert.assertEquals(1L, codigo.longValue());
   }

   @Test
   public void testBusca_ComCodigo() {
      VendaFilter filter = new VendaFilter();
      filter.setCodigo(1L);
      this.vendaService.busca(filter, "ABERTA", Pageable.unpaged());
      ((VendaRepository)Mockito.verify(this.vendas)).findByCodigoIn((Long)ArgumentMatchers.eq(filter.getCodigo()), (Pageable)ArgumentMatchers.any());
   }

   @Test
   public void testBusca_SemCodigo() {
      VendaFilter filter = new VendaFilter();
      this.vendaService.busca(filter, "FECHADA", Pageable.unpaged());
      ((VendaRepository)Mockito.verify(this.vendas)).findBySituacaoEquals((VendaSituacao)ArgumentMatchers.eq(VendaSituacao.FECHADA), (Pageable)ArgumentMatchers.any());
   }

   @Test
   public void testAddProduto_VendaAberta() {
      Mockito.when(this.vendas.verificaSituacao(1L)).thenReturn(VendaSituacao.ABERTA.toString());
      String result = this.vendaService.addProduto(1L, 2L, (double)10.0F);
      ArgumentCaptor<VendaProduto> produto = ArgumentCaptor.forClass(VendaProduto.class);
      ((VendaProdutoService)Mockito.verify(this.vendaProdutos)).salvar((VendaProduto)produto.capture());
      Assert.assertEquals(1L, ((VendaProduto)produto.getValue()).getVenda().longValue());
      Assert.assertEquals(2L, ((VendaProduto)produto.getValue()).getProduto().longValue());
      Assert.assertEquals(10.0, ((VendaProduto)produto.getValue()).getValor_balanca(), 0.0);
      Assert.assertEquals("ok", result);
   }

   @Test
   public void testAddProduto_VendaFechada() {
      Mockito.when(this.vendas.verificaSituacao(1L)).thenReturn(VendaSituacao.FECHADA.toString());
      String result = this.vendaService.addProduto(1L, 2L, (double)10.0F);
      Assert.assertEquals("Venda fechada", result);
   }

   @Test
   public void testRemoveProduto_VendaAberta() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      String result = this.vendaService.removeProduto(1L, 1L);
      ((VendaRepository)Mockito.verify(this.vendas)).findByCodigoEquals(1L);
      ((VendaProdutoService)Mockito.verify(this.vendaProdutos)).removeProduto(1L);
      Assert.assertEquals("ok", result);
   }

   @Test
   public void testRemoveProduto_VendaFechada() {
      this.vendaMock.setSituacao(VendaSituacao.FECHADA);
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      String result = this.vendaService.removeProduto(1L, 1L);
      Assert.assertEquals("Venda fechada", result);
   }

   @Test
   public void testLista() {
      Mockito.when(this.vendas.findAll()).thenReturn(Collections.singletonList(this.vendaMock));
      Assert.assertEquals(1L, (long)this.vendaService.lista().size());
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_VendaFechada_LancaExcecao() {
      this.vendaMock.setSituacao(VendaSituacao.FECHADA);
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[0], new String[0]);
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_ValorZero_LancaExcecao() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      this.vendaService.fechaVenda(1L, 1L, (double)0.0F, (double)0.0F, (double)0.0F, new String[0], new String[0]);
   }

   @Test
   public void testFechaVenda_AvistaCartaoCredito() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagTipo = new PagamentoTipo();
      pagTipo.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagTipo);
      TituloTipo tipoTit = new TituloTipo();
      tipoTit.setSigla(net.originmobi.pdv.enumerado.TituloTipo.CARTCRED.toString());
      Titulo titulo = new Titulo();
      titulo.setTipo(tipoTit);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(titulo));
      String[] vlParcelas = new String[]{"100.0"};
      String[] titulos = new String[]{"2"};
      String result = this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, vlParcelas, titulos);
      ArgumentCaptor<Double> valor = ArgumentCaptor.forClass(Double.class);
      ((CartaoLancamentoService)Mockito.verify(this.cartaoLancamento)).lancamento((Double)valor.capture(), (Optional)ArgumentMatchers.eq(Optional.of(titulo)));
      ((VendaRepository)Mockito.verify(this.vendas)).fechaVenda(ArgumentMatchers.eq(1L), (VendaSituacao)ArgumentMatchers.eq(VendaSituacao.FECHADA), ArgumentMatchers.eq((double)100.0F), ArgumentMatchers.eq((double)0.0F), ArgumentMatchers.eq((double)0.0F), (Timestamp)ArgumentMatchers.any(Timestamp.class), (PagamentoTipo)ArgumentMatchers.eq(pagTipo));
      ((ProdutoService)Mockito.verify(this.produtos)).movimentaEstoque(ArgumentMatchers.eq(1L), (EntradaSaida)ArgumentMatchers.eq(EntradaSaida.SAIDA));
      ((ParcelaService)Mockito.verify(this.parcelas, Mockito.never())).gerarParcela(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Receber)ArgumentMatchers.any(Receber.class), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (Date)ArgumentMatchers.any(Date.class));
      Assert.assertEquals(100.0, valor.getValue(), 0.0);
      Assert.assertEquals("Venda finalizada com sucesso", result);
   }

   @Test
   public void testFechaVenda_AvistaCartaoDebito() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      TituloTipo tipo = new TituloTipo();
      tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.CARTDEB.toString());
      Titulo titulo = new Titulo();
      titulo.setTipo(tipo);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(titulo));
      String result = this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ((CartaoLancamentoService)Mockito.verify(this.cartaoLancamento)).lancamento(ArgumentMatchers.eq((double)100.0F), (Optional)ArgumentMatchers.eq(Optional.of(titulo)));
      ((CaixaLancamentoService)Mockito.verify(this.lancamentos, Mockito.never())).lancamento((CaixaLancamento)ArgumentMatchers.any());
      ((ParcelaService)Mockito.verify(this.parcelas, Mockito.never())).gerarParcela(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Receber)ArgumentMatchers.any(Receber.class), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (Date)ArgumentMatchers.any(Date.class));
      ((ProdutoService)Mockito.verify(this.produtos)).movimentaEstoque(1L, EntradaSaida.SAIDA);
      Assert.assertEquals("Venda finalizada com sucesso", result);
   }

   @Test
   public void testFechaVenda_AvistaDinheiro_CaixaAberto() {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("usuario", (Object)null));
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      TituloTipo tipo = new TituloTipo();
      tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
      Titulo titulo = new Titulo();
      titulo.setTipo(tipo);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(titulo));
      Usuario usuario = new Usuario();
      Caixa caixa = new Caixa();
      Mockito.when(this.caixas.caixaIsAberto()).thenReturn(true);
      Mockito.when(this.caixas.caixaAberto()).thenReturn(Optional.of(caixa));
      Mockito.when(this.usuarios.buscaUsuario("usuario")).thenReturn(usuario);
      String result = this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ArgumentCaptor<CaixaLancamento> lancamento = ArgumentCaptor.forClass(CaixaLancamento.class);
      ((CaixaLancamentoService)Mockito.verify(this.lancamentos)).lancamento((CaixaLancamento)lancamento.capture());
      Assert.assertEquals(100.0, ((CaixaLancamento)lancamento.getValue()).getValor(), 0.0);
      Assert.assertEquals(TipoLancamento.RECEBIMENTO, ((CaixaLancamento)lancamento.getValue()).getTipo());
      Assert.assertEquals(EstiloLancamento.ENTRADA, ((CaixaLancamento)lancamento.getValue()).getEstilo());
      Assert.assertEquals(caixa, ((CaixaLancamento)lancamento.getValue()).getCaixa().get());
      Assert.assertEquals(usuario, ((CaixaLancamento)lancamento.getValue()).getUsuario());
      ((CartaoLancamentoService)Mockito.verify(this.cartaoLancamento, Mockito.never())).lancamento(ArgumentMatchers.anyDouble(), (Optional)ArgumentMatchers.any());
      ((ParcelaService)Mockito.verify(this.parcelas, Mockito.never())).gerarParcela(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Receber)ArgumentMatchers.any(Receber.class), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (Date)ArgumentMatchers.any(Date.class));
      ((ProdutoService)Mockito.verify(this.produtos)).movimentaEstoque(1L, EntradaSaida.SAIDA);
      Assert.assertEquals("Venda finalizada com sucesso", result);
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_AvistaDinheiro_CaixaFechado() {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("usuario", (Object)null));
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      TituloTipo tipo = new TituloTipo();
      tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
      Titulo titulo = new Titulo();
      titulo.setTipo(tipo);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(titulo));
      Mockito.when(this.caixas.caixaIsAberto()).thenReturn(false);
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ((CaixaLancamentoService)Mockito.verify(this.lancamentos, Mockito.never())).lancamento((CaixaLancamento)ArgumentMatchers.any());
      ((VendaRepository)Mockito.verify(this.vendas, Mockito.never())).fechaVenda(ArgumentMatchers.anyLong(), (VendaSituacao)ArgumentMatchers.any(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (PagamentoTipo)ArgumentMatchers.any());
      ((ProdutoService)Mockito.verify(this.produtos, Mockito.never())).movimentaEstoque(ArgumentMatchers.anyLong(), (EntradaSaida)ArgumentMatchers.any(EntradaSaida.class));
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_AvistaDinheiro_ParcelaVazia() {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("usuario", (Object)null));
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      TituloTipo tipo = new TituloTipo();
      tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
      Titulo titulo = new Titulo();
      titulo.setTipo(tipo);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(titulo));
      Mockito.when(this.caixas.caixaIsAberto()).thenReturn(true);
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{""}, new String[]{"2"});
      ((CaixaLancamentoService)Mockito.verify(this.lancamentos, Mockito.never())).lancamento((CaixaLancamento)ArgumentMatchers.any());
      ((ProdutoService)Mockito.verify(this.produtos, Mockito.never())).movimentaEstoque(ArgumentMatchers.anyLong(), (EntradaSaida)ArgumentMatchers.any(EntradaSaida.class));
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_AvistaDinheiro_ParcelasDivergentes() {
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("usuario", (Object)null));
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      TituloTipo tipo = new TituloTipo();
      tipo.setSigla(net.originmobi.pdv.enumerado.TituloTipo.DIN.toString());
      Titulo titulo = new Titulo();
      titulo.setTipo(tipo);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(titulo));
      Mockito.when(this.caixas.caixaIsAberto()).thenReturn(true);
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"90.0"}, new String[]{"2"});
      ((CaixaLancamentoService)Mockito.verify(this.lancamentos, Mockito.never())).lancamento((CaixaLancamento)ArgumentMatchers.any());
      ((ProdutoService)Mockito.verify(this.produtos, Mockito.never())).movimentaEstoque(ArgumentMatchers.anyLong(), (EntradaSaida)ArgumentMatchers.any(EntradaSaida.class));
   }

   @Test
   public void testFechaVenda_Aprazo_GeraParcela() {
      this.vendaMock.setPessoa(new Pessoa());
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("30");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(new Titulo()));
      String result = this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ((ParcelaService)Mockito.verify(this.parcelas)).gerarParcela(ArgumentMatchers.eq((double)100.0F), ArgumentMatchers.eq((double)0.0F), ArgumentMatchers.eq((double)0.0F), ArgumentMatchers.eq((double)0.0F), ArgumentMatchers.eq((double)100.0F), (Receber)ArgumentMatchers.any(Receber.class), ArgumentMatchers.eq(0), ArgumentMatchers.eq(1), (Timestamp)ArgumentMatchers.any(Timestamp.class), (Date)ArgumentMatchers.any(Date.class));
      ((CartaoLancamentoService)Mockito.verify(this.cartaoLancamento, Mockito.never())).lancamento(ArgumentMatchers.anyDouble(), (Optional)ArgumentMatchers.any());
      ((CaixaLancamentoService)Mockito.verify(this.lancamentos, Mockito.never())).lancamento((CaixaLancamento)ArgumentMatchers.any());
      ((ProdutoService)Mockito.verify(this.produtos)).movimentaEstoque(1L, EntradaSaida.SAIDA);
      Assert.assertEquals("Venda finalizada com sucesso", result);
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_Aprazo_SemCliente() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("30");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.of(new Titulo()));
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ((ParcelaService)Mockito.verify(this.parcelas, Mockito.never())).gerarParcela(ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Receber)ArgumentMatchers.any(Receber.class), ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (Date)ArgumentMatchers.any(Date.class));
      ((ProdutoService)Mockito.verify(this.produtos, Mockito.never())).movimentaEstoque(ArgumentMatchers.anyLong(), (EntradaSaida)ArgumentMatchers.any(EntradaSaida.class));
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_ErroAoCadastrarReceber_NaoFechaVenda() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      ((ReceberService)Mockito.doThrow(new Throwable[]{new IllegalStateException("falha")}).when(this.receberServ)).cadastrar((Receber)ArgumentMatchers.any(Receber.class));
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ((VendaRepository)Mockito.verify(this.vendas, Mockito.never())).fechaVenda(ArgumentMatchers.anyLong(), (VendaSituacao)ArgumentMatchers.any(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (PagamentoTipo)ArgumentMatchers.any());
      ((ProdutoService)Mockito.verify(this.produtos, Mockito.never())).movimentaEstoque(ArgumentMatchers.anyLong(), (EntradaSaida)ArgumentMatchers.any(EntradaSaida.class));
   }

   @Test(
      expected = RuntimeException.class
   )
   public void testFechaVenda_TituloInexistente_NaoFechaVenda() {
      Mockito.when(this.vendas.findByCodigoEquals(1L)).thenReturn(this.vendaMock);
      PagamentoTipo pagamento = new PagamentoTipo();
      pagamento.setFormaPagamento("00");
      Mockito.when(this.formaPagamentos.busca(1L)).thenReturn(pagamento);
      Mockito.when(this.tituloService.busca(2L)).thenReturn(Optional.empty());
      this.vendaService.fechaVenda(1L, 1L, (double)100.0F, (double)0.0F, (double)0.0F, new String[]{"100.0"}, new String[]{"2"});
      ((VendaRepository)Mockito.verify(this.vendas, Mockito.never())).fechaVenda(ArgumentMatchers.anyLong(), (VendaSituacao)ArgumentMatchers.any(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyDouble(), (Timestamp)ArgumentMatchers.any(Timestamp.class), (PagamentoTipo)ArgumentMatchers.any());
      ((ProdutoService)Mockito.verify(this.produtos, Mockito.never())).movimentaEstoque(ArgumentMatchers.anyLong(), (EntradaSaida)ArgumentMatchers.any(EntradaSaida.class));
   }

   @Test
   public void testQtdAbertos() {
      Mockito.when(this.vendas.qtdVendasEmAberto()).thenReturn(5);
      Assert.assertEquals(5L, (long)this.vendaService.qtdAbertos());
   }
}
