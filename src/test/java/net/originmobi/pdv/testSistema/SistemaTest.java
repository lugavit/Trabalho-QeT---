package net.originmobi.pdv.testSistema;

import java.util.List;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SistemaTest {
    
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void iniciardriver() {
        WebDriverManager.firefoxdriver().driverVersion("0.37.0").setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("app.update.enabled", false);
        options.addPreference("app.update.auto", false);
        options.addPreference("app.update.mode", 0);
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 10);
        driver.get("http://localhost:8080/login");
    }

    @AfterEach
    public void fechardriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "Boneco, 10.00, 15.00, UN, 2023-01-01",
            "Carrinho de Bebê, 50.00, 75.00, UN, 2023-01-02"
    })
    public void CadastrarProdutoComIsencao(String Descricao, String custo, String venda, String unidade, String validade) {
        PaginaLogin paginaLogin = new PaginaLogin(driver, wait);
        paginaLogin.FazerLogin("gerente", "123");

        PaginaPrincipal paginaPrincipal = new PaginaPrincipal(driver, wait);
        paginaPrincipal.clicarBotaoProdutoProduto();

        PaginaProduto paginaProduto = new PaginaProduto(driver, wait);
        paginaProduto.CriarNovoProduto(Descricao, custo, venda, unidade, validade);

        assertTrue(paginaProduto.verificarMensagemSucesso(), "Mensagem de sucesso não foi exibida");
        assertTrue(paginaProduto.verificarProdutoNaLista(Descricao), "Produto não foi encontrado na lista");
    }
}
