package net.originmobi.pdv.testSistema;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    public void iniciarDriver() {
        WebDriverManager.firefoxdriver().driverVersion("0.37.0").setup();
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.addPreference("app.update.enabled", false);
        firefoxOptions.addPreference("app.update.auto", false);
        firefoxOptions.addPreference("app.update.mode", 0);

        driver = new FirefoxDriver(firefoxOptions);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 10);
        driver.get("http://localhost:8080/login");
    }

    @AfterEach
    public void fecharDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "Boneco, 10.00, 15.00, UN, 2023-01-01",
            "Carrinho de Bebê, 50.00, 75.00, UN, 2023-01-02"
    })
    public void cadastrarProdutoComIsencao(
            String descricao, String custo, String venda, String unidade, String validade) {
        PaginaLogin paginaLogin = new PaginaLogin(driver, wait);
        paginaLogin.FazerLogin("gerente", "123");

        PaginaPrincipal paginaPrincipal = new PaginaPrincipal(driver, wait);
        paginaPrincipal.clicarBotaoProdutoProduto();

        PaginaProduto paginaProduto = new PaginaProduto(driver, wait);
        paginaProduto.CriarNovoProduto(descricao, custo, venda, unidade, validade);

        assertTrue(paginaProduto.verificarMensagemSucesso(), "Mensagem de sucesso não foi exibida");
        assertTrue(paginaProduto.verificarProdutoNaLista(descricao), "Produto não foi encontrado na lista");
    }
}
