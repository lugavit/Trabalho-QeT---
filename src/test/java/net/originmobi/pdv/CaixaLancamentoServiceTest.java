package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaLancamentoRepository;
import net.originmobi.pdv.service.CaixaLancamentoService;
import net.originmobi.pdv.service.UsuarioService;

@RunWith(MockitoJUnitRunner.class)
public class CaixaLancamentoServiceTest {

    @Mock
    private CaixaLancamentoRepository caixaLancamentoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CaixaLancamentoService caixaLancamentoService;

    private Caixa caixaAberto;
    private Usuario usuario;

    @Before
    public void setUp() {
        usuario = new Usuario();
        usuario.setCodigo(1L);
        usuario.setUser("mateus");

        caixaAberto = new Caixa();
        caixaAberto.setCodigo(100L);
        caixaAberto.setValor_total(500.0);
        caixaAberto.setUsuario(usuario);
    }

    @Test
    public void testLancamentoEntradaSucesso() {
        CaixaLancamento lancamento = new CaixaLancamento("Entrada de troco", 100.0,
                TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, caixaAberto, usuario);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertNotNull(lancamento.getData_cadastro());
        verify(caixaLancamentoRepository, times(1)).save(lancamento);
    }

    @Test
    public void testLancamentoSaidaComSaldoSuficienteConverteParaNegativo() {
        CaixaLancamento lancamento = new CaixaLancamento("Sangria dinheiro", 200.0,
                TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, caixaAberto, usuario);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertTrue(lancamento.getValor() < 0);
        assertEquals(Double.valueOf(-200.0), lancamento.getValor());
        verify(caixaLancamentoRepository, times(1)).save(lancamento);
    }

    @Test
    public void testLancamentoSaidaSaldoInsuficiente() {
        CaixaLancamento lancamento = new CaixaLancamento("Sangria excessiva", 600.0,
                TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, caixaAberto, usuario);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Saldo insuficiente para realizar esta operação", resultado);
        verify(caixaLancamentoRepository, never()).save(any(CaixaLancamento.class));
    }

    @Test
    public void testLancamentoPreencheObservacaoPadraoParaSangriaSeVazia() {
        CaixaLancamento lancamento = new CaixaLancamento("", 50.0,
                TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, caixaAberto, usuario);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("Sangria de caixa", lancamento.getObservacao());
        verify(caixaLancamentoRepository, times(1)).save(lancamento);
    }

    @Test
    public void testLancamentoPreencheObservacaoPadraoParaSuprimentoSeVazia() {
        CaixaLancamento lancamento = new CaixaLancamento("", 80.0,
                TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, caixaAberto, usuario);

        String resultado = caixaLancamentoService.lancamento(lancamento);

        assertEquals("Lançamento realizado com sucesso", resultado);
        assertEquals("Suprimento de caixa", lancamento.getObservacao());
        verify(caixaLancamentoRepository, times(1)).save(lancamento);
    }

    @Test
    public void testLancamentoMantemObservacaoExistente() {
        CaixaLancamento lancamento = new CaixaLancamento("Observação personalizada do operador", 50.0,
                TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, caixaAberto, usuario);

        caixaLancamentoService.lancamento(lancamento);

        assertEquals("Observação personalizada do operador", lancamento.getObservacao());
        verify(caixaLancamentoRepository, times(1)).save(lancamento);
    }

    @Test(expected = RuntimeException.class)
    public void testLancamentoErroAoPersistirLancaExcecao() {
        CaixaLancamento lancamento = new CaixaLancamento("Suprimento", 50.0,
                TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, caixaAberto, usuario);

        when(caixaLancamentoRepository.save(any(CaixaLancamento.class)))
                .thenThrow(new RuntimeException("Database error"));

        caixaLancamentoService.lancamento(lancamento);
    }

    @Test
    public void testLancamentosDoCaixaSucesso() {
        CaixaLancamento l1 = new CaixaLancamento("L1", 50.0, TipoLancamento.SUPRIMENTO, EstiloLancamento.ENTRADA, caixaAberto, usuario);
        CaixaLancamento l2 = new CaixaLancamento("L2", -20.0, TipoLancamento.SANGRIA, EstiloLancamento.SAIDA, caixaAberto, usuario);
        List<CaixaLancamento> listaEsperada = Arrays.asList(l1, l2);

        when(caixaLancamentoRepository.findByCaixaEquals(caixaAberto)).thenReturn(listaEsperada);

        List<CaixaLancamento> retorno = caixaLancamentoService.lancamentosDoCaixa(caixaAberto);

        assertNotNull(retorno);
        assertEquals(2, retorno.size());
        verify(caixaLancamentoRepository, times(1)).findByCaixaEquals(caixaAberto);
    }
}