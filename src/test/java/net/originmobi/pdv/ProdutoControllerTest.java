package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.ModelAndView;

import net.originmobi.pdv.controller.ProdutoController;
import net.originmobi.pdv.enumerado.produto.ProdutoSubstTributaria;
import net.originmobi.pdv.filter.ProdutoFilter;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.service.CategoriaService;
import net.originmobi.pdv.service.FornecedorService;
import net.originmobi.pdv.service.GrupoService;
import net.originmobi.pdv.service.ImagemProdutoService;
import net.originmobi.pdv.service.ProdutoService;
import net.originmobi.pdv.service.TributacaoService;
import net.originmobi.pdv.service.notafiscal.ModBcIcmsService;

@RunWith(MockitoJUnitRunner.class)
public class ProdutoControllerTest {

    @InjectMocks
    private ProdutoController controller;

    @Mock
    private ProdutoService produtos;

    @Mock
    private FornecedorService fornecedores;

    @Mock
    private GrupoService grupos;

    @Mock
    private CategoriaService categorias;

    @Mock
    private ImagemProdutoService imagens;

    @Mock
    private TributacaoService tributacoes;

    @Mock
    private ModBcIcmsService modbcs;

    @Test
    public void cadastrarConverteValoresComSeparadorDeMilhar() {
        Map<String, String> request = new HashMap<>();
        request.put("codigo", "");
        request.put("descricao", "Console");
        request.put("fornecedor", "1");
        request.put("categoria", "2");
        request.put("grupo", "3");
        request.put("balanca", "NAO");
        request.put("valor_custo", "3.000,00");
        request.put("valor_venda", "4.400,00");
        request.put("data_validade", "");
        request.put("controla_estoque", "SIM");
        request.put("ativo", "ATIVO");
        request.put("unidade", "UN");
        request.put("subtributaria", "NAO");
        request.put("ncm", "");
        request.put("cest", "");
        request.put("tributacao", "");
        request.put("modBcIcms", "");
        request.put("vendavel", "SIM");
        RedirectAttributes attributes = new RedirectAttributesModelMap();

        String redirect = controller.cadastrar(request, attributes);

        ArgumentCaptor<Double> valorCusto = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> valorVenda = ArgumentCaptor.forClass(Double.class);
        verify(produtos).merger(eq(0L), eq(1L), eq(2L), eq(3L), eq(0), eq("Console"),
                valorCusto.capture(), valorVenda.capture(), isNull(), eq("SIM"), eq("ATIVO"),
                eq("UN"), eq(ProdutoSubstTributaria.NAO), eq(""), eq(""), isNull(), isNull(),
                eq("SIM"));

        assertEquals(3000.0, valorCusto.getValue(), 0.0);
        assertEquals(4400.0, valorVenda.getValue(), 0.0);
        assertEquals("redirect:/produto", redirect);
    }

    @Test
    public void cadastrarAtualizaProdutoComTodosOsRamosAlternativos() {
        Map<String, String> request = requestBase();
        request.put("codigo", "10");
        request.put("balanca", "SIM");
        request.put("valor_custo", null);
        request.put("valor_venda", " ");
        request.put("data_validade", "21/11/2026");
        request.put("ativo", "INATIVO");
        request.put("subtributaria", "SIM");
        request.put("tributacao", "7");
        request.put("modBcIcms", "8");

        String redirect = controller.cadastrar(request, new RedirectAttributesModelMap());

        ArgumentCaptor<java.util.Date> validade = ArgumentCaptor.forClass(java.util.Date.class);
        verify(produtos).merger(eq(10L), eq(1L), eq(2L), eq(3L), eq(1), eq("Console"),
                eq(0.0), eq(0.0), validade.capture(), eq("SIM"), eq("INATIVO"),
                eq("UN"), eq(ProdutoSubstTributaria.SIM), eq(""), eq(""), eq(7L), eq(8L),
                eq("SIM"));
        assertNotNull(validade.getValue());
        assertEquals("redirect:/produto/10", redirect);
    }

    @Test
    public void cadastrarMantemValoresPadraoQuandoRequestEstaSemValoresOpcionais() {
        Map<String, String> request = requestBase();
        request.put("data_validade", "data-invalida");
        request.put("valor_custo", "");
        request.put("valor_venda", "");
        request.put("tributacao", "");
        request.put("modBcIcms", "");

        String redirect = controller.cadastrar(request, new RedirectAttributesModelMap());

        verify(produtos).merger(eq(0L), eq(1L), eq(2L), eq(3L), eq(0), eq("Console"),
                eq(0.0), eq(0.0), isNull(), eq("SIM"), eq("ATIVO"),
                eq("UN"), eq(ProdutoSubstTributaria.NAO), eq(""), eq(""), isNull(), isNull(),
                eq("SIM"));
        assertEquals("redirect:/produto", redirect);
    }

    @Test
    public void cadastrarAceitaValorDecimalSemSeparadorDeMilhar() {
        Map<String, String> request = requestBase();
        request.put("valor_custo", "123.45");
        request.put("valor_venda", "250");

        controller.cadastrar(request, new RedirectAttributesModelMap());

        ArgumentCaptor<Double> valorCusto = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> valorVenda = ArgumentCaptor.forClass(Double.class);
        verify(produtos).merger(eq(0L), eq(1L), eq(2L), eq(3L), eq(0), eq("Console"),
                valorCusto.capture(), valorVenda.capture(), isNull(), eq("SIM"), eq("ATIVO"),
                eq("UN"), eq(ProdutoSubstTributaria.NAO), eq(""), eq(""), isNull(), isNull(),
                eq("SIM"));
        assertEquals(123.45, valorCusto.getValue(), 0.0);
        assertEquals(250.0, valorVenda.getValue(), 0.0);
    }

    @Test
    public void formListaInicializadorListaPaginadaEEditar() {
        Produto novo = new Produto();
        ModelAndView form = controller.form(novo);
        assertEquals("produto/form", form.getViewName());
        assertNotNull(form.getModel().get("produto"));

        ProdutoFilter filter = controller.inicializerProdutoFilter();
        assertNotNull(filter);

        ProdutoFilter filtroAtivo = new ProdutoFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(java.util.Collections.singletonList(novo), pageable, 1);
        when(produtos.filter(filtroAtivo, pageable)).thenReturn(pagina);
        ExtendedModelMap model = new ExtendedModelMap();

        ModelAndView lista = controller.lista(filtroAtivo, pageable, model);
        assertEquals("produto/list", lista.getViewName());
        assertSame(pagina, lista.getModel().get("produtos"));
        assertTrue(model.containsAttribute("qtdpaginas"));

        when(imagens.busca(10L)).thenReturn(null);
        Produto editado = new Produto();
        editado.setCodigo(10L);
        ModelAndView editar = controller.editar(editado);
        assertEquals("produto/form", editar.getViewName());
        assertSame(editado, editar.getModel().get("produto"));
        verify(imagens).busca(10L);
    }

    @Test
    public void metodosDeModeloRetornamEnumsEServicos() throws Exception {
        assertNotNull(controller.ativo());
        assertNotNull(controller.fornecedores());
        assertNotNull(controller.grupos());
        assertNotNull(controller.categorias());
        assertNotNull(controller.balanca());
        assertNotNull(controller.substTributaria());
        assertNotNull(controller.tributacoes());
        assertNotNull(controller.produtoVendavel());

        java.lang.reflect.Method modbc = ProdutoController.class.getDeclaredMethod("modbc");
        modbc.setAccessible(true);
        assertNotNull(modbc.invoke(controller));
        java.lang.reflect.Method controlaestoque = ProdutoController.class.getDeclaredMethod("controlaestoque");
        controlaestoque.setAccessible(true);
        assertNotNull(controlaestoque.invoke(controller));

        verify(fornecedores).lista();
        verify(grupos).lista();
        verify(categorias).lista();
        verify(tributacoes).lista();
        verify(modbcs).lista();
    }

    private Map<String, String> requestBase() {
        Map<String, String> request = new HashMap<>();
        request.put("codigo", "");
        request.put("descricao", "Console");
        request.put("fornecedor", "1");
        request.put("categoria", "2");
        request.put("grupo", "3");
        request.put("balanca", "NAO");
        request.put("valor_custo", "100,00");
        request.put("valor_venda", "200,00");
        request.put("data_validade", "");
        request.put("controla_estoque", "SIM");
        request.put("ativo", "ATIVO");
        request.put("unidade", "UN");
        request.put("subtributaria", "NAO");
        request.put("ncm", "");
        request.put("cest", "");
        request.put("tributacao", "");
        request.put("modBcIcms", "");
        request.put("vendavel", "SIM");
        return request;
    }
}
