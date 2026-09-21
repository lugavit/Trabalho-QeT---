package net.originmobi.pdv;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
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
        // Simulando uma senha de banco já criptografada para passar pela validação do fechaCaixa
        usuarioMock.setSenha(encoder.encode("senha123")); 
        
        // Simula o retorno de qualquer usuário solicitado pela Aplicação (Singleton)
        when(usuarios.buscaUsuario(any())).thenReturn(usuarioMock);
    }

    // =========================================================================
    // CATEGORIA 1: CAMINHOS FELIZES
    // =========================================================================

    @Test
    public void testCadastro_CaixaComSucesso_ValorMaiorQueZero() {
        Caixa caixa = new Caixa();
        caixa.setCodigo(10L);
        caixa.setTipo(CaixaTipo.CAIXA);
        caixa.setDescricao(""); // Deve assumir "Caixa diário"
        caixa.setValor_abertura(100.0);

        // Simulando que não há outro caixa aberto
        when(caixas.caixaAberto()).thenReturn(Optional.empty());

        Long codigoGerado = caixaService.cadastro(caixa);

        // Validações
        assertEquals("Caixa diário", caixa.getDescricao());
        assertEquals(Double.valueOf(100.0), caixa.getValor_abertura());
        
        // Verifica se persistiu o caixa e gerou o lançamento no CaixaLancamentoService
        verify(caixas, times(1)).save(caixa);
        verify(lancamentos, times(1)).lancamento(any(CaixaLancamento.class));
        assertEquals(Long.valueOf(10L), codigoGerado);
    }

    @Test
    public void testCadastro_CaixaComValorAberturaNulo_DeveSetarZero() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.CAIXA);
        caixa.setDescricao("Meu Caixa");
        caixa.setValor_abertura(null); // Vai ser convertido para 0.0 pelo operador ternário

        when(caixas.caixaAberto()).thenReturn(Optional.empty());

        caixaService.cadastro(caixa);

        assertEquals(Double.valueOf(0.0), caixa.getValor_abertura());
        assertEquals(Double.valueOf(0.0), caixa.getValor_total()); // Como não há lançamento, zera o total
        
        // Verifica que o lançamento inicial NÃO foi gerado (pois o valor não é > 0)
        verify(lancamentos, never()).lancamento(any(CaixaLancamento.class));
    }

    @Test
    public void testFechaCaixa_ComSucesso() {
        Caixa caixaAberto = new Caixa();
        caixaAberto.setCodigo(1L);
        caixaAberto.setValor_total(500.0);
        // Sem data de fechamento, significa que está aberto

        when(caixas.findById(1L)).thenReturn(Optional.of(caixaAberto));

        String resposta = caixaService.fechaCaixa(1L, "senha123");

        assertEquals("Caixa fechado com sucesso", resposta);
        assertNotNull(caixaAberto.getData_fechamento()); // Validando se o timestamp de fecho foi preenchido
        assertEquals(Double.valueOf(500.0), caixaAberto.getValor_fechamento());
        verify(caixas, times(1)).save(caixaAberto);
    }

    // =========================================================================
    // CATEGORIA 2: REGRAS DE NEGÓCIO E EXCEÇÕES
    // =========================================================================

    @Test
    public void testCadastro_CaixaJaAberto_LancaExcecao() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.CAIXA);

        // Simulando que JÁ EXISTE um caixa aberto
        when(caixas.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        try {
            caixaService.cadastro(caixa);
            fail("Deveria ter lançado RuntimeException bloqueando abertura simultânea");
        } catch (RuntimeException e) {
            assertEquals("Existe caixa de dias anteriores em aberto, favor verifique", e.getMessage());
        }
        
        // Garante que o método save() nunca foi tocado
        verify(caixas, never()).save(any());
    }

    @Test
    public void testCadastro_ValorNegativo_LancaExcecao() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.COFRE);
        caixa.setValor_abertura(-50.0); // Valor inválido

        try {
            caixaService.cadastro(caixa);
            fail("Deveria ter lançado RuntimeException para valor negativo");
        } catch (RuntimeException e) {
            assertEquals("Valor informado é inválido", e.getMessage());
        }
        verify(caixas, never()).save(any());
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
        caixaFechado.setData_fechamento(new Timestamp(System.currentTimeMillis())); // Registra data de fechamento

        when(caixas.findById(1L)).thenReturn(Optional.of(caixaFechado));

        try {
            caixaService.fechaCaixa(1L, "senha123");
            fail("Deveria ter lançado RuntimeException pois caixa já estava fechado");
        } catch (RuntimeException e) {
            assertEquals("Caixa já esta fechado", e.getMessage());
        }
    }

    // =========================================================================
    // CATEGORIA 3: EXPOSIÇÃO DE BUGS DA APLICAÇÃO (TESTES BASEADOS EM DEFEITOS)
    // =========================================================================

    @Test
    public void testCadastro_BugExposto_BancoComAgenciaNula_LancaNullPointerException() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.BANCO);
        caixa.setValor_abertura(100.0);
        caixa.setDescricao("Banco Teste");
        // BUG: Usuario não informou a agência (null). 
        // O código não prevê isso e tenta dar um .replaceAll()
        caixa.setAgencia(null); 
        caixa.setConta("12345");

        try {
            caixaService.cadastro(caixa);
            fail("Deveria ter lançado NullPointerException pelo erro não tratado do desenvolvedor");
        } catch (NullPointerException e) {
            // Este teste prova que o sistema "quebra" feio aqui ao invés de retornar uma 
            // RuntimeException amigável exigindo o preenchimento da Agência.
            assertTrue(true);
        }
    }

    @Test
    public void testFechaCaixa_BugExposto_CaixaInexistente_LancaNoSuchElementException() {
        // Simulamos o retorno do banco VAZIO (o ID informado não existe)
        when(caixas.findById(999L)).thenReturn(Optional.empty());

        try {
            caixaService.fechaCaixa(999L, "senha123");
            fail("Deveria ter lançado NoSuchElementException pelo uso inadequado de Optional.get()");
        } catch (NoSuchElementException e) {
            // BUG provado: Como o programador fez "caixaAtual.get().setData_fechamento()" 
            // sem usar if (caixaAtual.isPresent()), o Java estoura erro interno (Code 500)
            assertTrue(true);
        }
    }
}