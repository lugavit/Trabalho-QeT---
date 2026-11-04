package net.originmobi.pdv.testSistema;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes de Sistema - Módulo Empresa (RFs e RNF Segurança)")
public class EmpresaServiceSistemaTest {

    private static final String BASE_URL = "http://localhost:8080";

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void iniciarNavegador() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("app.update.enabled", false);
        options.addPreference("app.update.auto", false);

        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 10);
    }

    @AfterEach
    public void finalizarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("RNF Segurança: Redirecionar para login ao tentar acessar /empresa sem autenticação")
    public void naoDevePermitirAcessoSemAutenticacao() {
        driver.get(BASE_URL + "/empresa");

        wait.until(ExpectedConditions.urlContains("/login"));

        String urlAtual = driver.getCurrentUrl();
        assertTrue(urlAtual.contains("/login"),
                "O sistema deveria ter bloqueado o acesso e redirecionado para a tela de login.");
    }

    @Test
    @DisplayName("RF 1: Deve permitir acesso e carregar a tela de cadastro/configuração da empresa após login")
    public void deveAcessarTelaDeCadastroEmpresaComSucesso() {
        fazerLoginENavegarParaEmpresa();

        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form_empresa")));
        WebElement campoNome = driver.findElement(By.id("nome"));
        WebElement campoCnpj = driver.findElement(By.id("cnpj"));
        WebElement botaoEnviar = driver.findElement(By.cssSelector("input[name='enviar']"));

        assertTrue(form.isDisplayed(), "O formulário da empresa deve estar visível.");
        assertTrue(campoNome.isDisplayed(), "O campo Nome da empresa deve estar visível.");
        assertTrue(campoCnpj.isDisplayed(), "O campo CNPJ deve estar visível.");
        assertTrue(botaoEnviar.isDisplayed(), "O botão de enviar deve estar visível na tela.");
    }

    @Test
    @DisplayName("RF 2: Não deve permitir atualizar cadastro quando o nome da empresa estiver em branco")
    public void naoDevePermitirEmpresaSemNome() {
        fazerLoginENavegarParaEmpresa();

        WebElement campoNome = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nome")));
        campoNome.clear();

        submeterFormulario();

        WebDriverWait waitCurto = new WebDriverWait(driver, 3);
        boolean possuiClasseErro = waitCurto.until(ExpectedConditions.attributeContains(campoNome, "class", "error"));

        assertTrue(possuiClasseErro, "O campo nome deveria apresentar o indicador visual de erro (class='error').");
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private void fazerLoginENavegarParaEmpresa() {
        driver.get(BASE_URL + "/login");
        new PaginaLogin(driver, wait).FazerLogin("gerente", "123");
        driver.get(BASE_URL + "/empresa");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("form_empresa")));
    }

    private void submeterFormulario() {
        WebElement botao = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[name='enviar']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", botao);
        wait.until(ExpectedConditions.elementToBeClickable(botao)).click();
    }
}
