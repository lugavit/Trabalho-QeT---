package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import net.originmobi.pdv.controller.ProdutoController;
import net.originmobi.pdv.enumerado.produto.ProdutoSubstTributaria;
import net.originmobi.pdv.service.ProdutoService;

@RunWith(MockitoJUnitRunner.class)
public class ProdutoControllerTest {

    @InjectMocks
    private ProdutoController controller;

    @Mock
    private ProdutoService produtos;

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
}
