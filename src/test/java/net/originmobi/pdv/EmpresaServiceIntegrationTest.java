package net.originmobi.pdv;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.junit4.SpringRunner;

import net.originmobi.pdv.service.EmpresaService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PdvApplication.class)
public class EmpresaServiceIntegrationTest {

    private static final String CNPJ = "04252011000110";
    private static final String MENSAGEM_SUCESSO = "Empresa salva com sucesso";

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    @After
    public void limparDadosDoTeste() {
        List<Map<String, Object>> empresas = jdbcTemplate.queryForList(
                "select endereco_codigo, parametro_codigo "
                        + "from empresa where cnpj = ?",
                CNPJ);

        jdbcTemplate.update("delete from empresa where cnpj = ?", CNPJ);

        for (Map<String, Object> empresa : empresas) {
            Number endereco = (Number) empresa.get("endereco_codigo");
            Number parametro = (Number) empresa.get("parametro_codigo");

            if (endereco != null) {
                jdbcTemplate.update(
                        "delete from endereco where codigo = ?",
                        endereco.longValue());
            }

            if (parametro != null) {
                jdbcTemplate.update(
                        "delete from empresa_parametros where codigo = ?",
                        parametro.longValue());
            }
        }
    }

    @Test
    public void deveCadastrarEmpresaComDadosValidosNoBanco() {
        String resultado = empresaService.merger(
                null,
                "Empresa Teste Integracao",
                "Empresa Teste",
                CNPJ,
                "123456",
                1,
                2,
                1L,
                null,
                2L,
                "Rua de Integracao",
                "Centro",
                "100",
                "76800000",
                "Perto da faculdade",
                2.50);

        assertEquals(MENSAGEM_SUCESSO, resultado);

        Map<String, Object> empresa = jdbcTemplate.queryForMap(
                "select nome, nome_fantasia, cnpj, ie, "
                        + "regime_tributario_codigo, parametro_codigo, endereco_codigo "
                        + "from empresa where cnpj = ?",
                CNPJ);

        assertEquals("Empresa Teste Integracao", empresa.get("nome"));
        assertEquals("Empresa Teste", empresa.get("nome_fantasia"));
        assertEquals(CNPJ, empresa.get("cnpj"));
        assertEquals("123456", empresa.get("ie"));
        assertEquals(1, ((Number) empresa.get("regime_tributario_codigo")).intValue());
        assertNotNull(empresa.get("parametro_codigo"));
        assertNotNull(empresa.get("endereco_codigo"));

        Map<String, Object> endereco = jdbcTemplate.queryForMap(
                "select rua, bairro, numero, cep, referencia "
                        + "from endereco where codigo = ?",
                empresa.get("endereco_codigo"));

        assertEquals("Rua de Integracao", endereco.get("rua"));
        assertEquals("Centro", endereco.get("bairro"));
        assertEquals("100", endereco.get("numero"));
        assertEquals("76800000", endereco.get("cep"));
        assertEquals("Perto da faculdade", endereco.get("referencia"));

        Map<String, Object> parametro = jdbcTemplate.queryForMap(
                "select serie_nfe, tipo_ambiente_codigo, p_credsn "
                        + "from empresa_parametros where codigo = ?",
                empresa.get("parametro_codigo"));

        assertEquals(1, ((Number) parametro.get("serie_nfe")).intValue());
        assertEquals(2, ((Number) parametro.get("tipo_ambiente_codigo")).intValue());
        assertEquals(2.50, ((Number) parametro.get("p_credsn")).doubleValue(), 0.001);
    }

    @Test
    public void deveAtualizarEmpresaEnderecoEParametros() {
        long parametroCodigo = inserirParametro();
        long enderecoCodigo = inserirEndereco();
        long empresaCodigo = inserirEmpresa(parametroCodigo, enderecoCodigo);

        String resultado = empresaService.merger(
                empresaCodigo,
                "Empresa Atualizada Integracao",
                "Empresa Atualizada",
                CNPJ,
                "654321",
                3,
                1,
                1L,
                enderecoCodigo,
                2L,
                "Rua Atualizada",
                "Bairro Atualizado",
                "999",
                "76800001",
                "Referencia Atualizada",
                4.75);

        assertEquals(MENSAGEM_SUCESSO, resultado);

        Map<String, Object> empresa = jdbcTemplate.queryForMap(
                "select nome, nome_fantasia, ie "
                        + "from empresa where codigo = ?",
                empresaCodigo);

        assertEquals("Empresa Atualizada Integracao", empresa.get("nome"));
        assertEquals("Empresa Atualizada", empresa.get("nome_fantasia"));
        assertEquals("654321", empresa.get("ie"));

        Map<String, Object> endereco = jdbcTemplate.queryForMap(
                "select rua, bairro, numero, cep, referencia "
                        + "from endereco where codigo = ?",
                enderecoCodigo);

        assertEquals("Rua Atualizada", endereco.get("rua"));
        assertEquals("Bairro Atualizado", endereco.get("bairro"));
        assertEquals("999", endereco.get("numero"));
        assertEquals("76800001", endereco.get("cep"));
        assertEquals("Referencia Atualizada", endereco.get("referencia"));

        Map<String, Object> parametro = jdbcTemplate.queryForMap(
                "select serie_nfe, tipo_ambiente_codigo, p_credsn "
                        + "from empresa_parametros where codigo = ?",
                parametroCodigo);

        assertEquals(3, ((Number) parametro.get("serie_nfe")).intValue());
        assertEquals(1, ((Number) parametro.get("tipo_ambiente_codigo")).intValue());
        assertEquals(4.75, ((Number) parametro.get("p_credsn")).doubleValue(), 0.001);
    }

    @Test
    public void deveRetornarEmpresaCadastrada() {
        long parametroCodigo = inserirParametro();
        long enderecoCodigo = inserirEndereco();
        inserirEmpresa(parametroCodigo, enderecoCodigo);

        assertTrue(empresaService.verificaEmpresaCadastrada().isPresent());
    }

    private long inserirParametro() {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into empresa_parametros "
                            + "(serie_nfe, tipo_ambiente_codigo, p_credsn) "
                            + "values (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, 1);
            statement.setInt(2, 2);
            statement.setDouble(3, 1.50);

            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private long inserirEndereco() {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into endereco "
                            + "(rua, bairro, numero, cep, referencia, "
                            + "data_cadastro, cidade_codigo) "
                            + "values (?, ?, ?, ?, ?, current_date, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, "Rua Fixture");
            statement.setString(2, "Bairro Fixture");
            statement.setString(3, "10");
            statement.setString(4, "76900000");
            statement.setString(5, "Referencia Fixture");
            statement.setLong(6, 2L);

            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private long inserirEmpresa(long parametroCodigo, long enderecoCodigo) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into empresa "
                            + "(nome, nome_fantasia, cnpj, ie, "
                            + "regime_tributario_codigo, parametro_codigo, endereco_codigo) "
                            + "values (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, "Empresa Fixture");
            statement.setString(2, "Fixture");
            statement.setString(3, CNPJ);
            statement.setString(4, "111111");
            statement.setLong(5, 1L);
            statement.setLong(6, parametroCodigo);
            statement.setLong(7, enderecoCodigo);

            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}




