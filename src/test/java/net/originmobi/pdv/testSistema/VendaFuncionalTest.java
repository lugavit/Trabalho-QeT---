package net.originmobi.pdv.testSistema;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class VendaFuncionalTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private final By clienteSelect = By.id("cliente");
    private final By salvarPedido = By.id("btn-salva");
    private final By codigoVenda = By.id("codigoVenda");
    private final By produtoSelect = By.id("codigoProduto");
    private final By inserirProduto = By.id("js-url");
    private final By tabelaProdutos = By.id("tabProdutoVenda");
    private final By totalProdutos = By.id("total_produtos");
    private final By campoPesquisa = By.id("codigo");

    @BeforeEach
    public void iniciarDriver() {
        WebDriverManager.firefoxdriver().driverVersion("0.37.0").setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("app.update.enabled", false);
        options.addPreference("app.update.auto", false);
        options.addPreference("app.update.mode", 0);

        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, 10);

        driver.get("http://localhost:8080/login");

        PaginaLogin paginaLogin = new PaginaLogin(driver, wait);
        paginaLogin.FazerLogin("gerente", "123");
    }

    @AfterEach
    public void fecharDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

   private String criarPedido() {
             driver.get("http://localhost:8080/venda/form");

             WebElement cliente = wait.until(
             ExpectedConditions.visibilityOfElementLocated(clienteSelect));

            new Select(cliente).selectByValue("1");

            wait.until(ExpectedConditions.elementToBeClickable(salvarPedido)).click();
            

           wait.until(driver -> {
                  WebElement campoCodigo = driver.findElement(codigoVenda);
                  String valor = campoCodigo.getAttribute("value");

                 return valor != null && !valor.trim().isEmpty();
           });
             System.out.println("URL após salvar: " + driver.getCurrentUrl());

            System.out.println("Conteúdo da página após salvar:");
            System.out.println(driver.findElement(By.tagName("body")).getText());

           String codigo = driver.findElement(codigoVenda).getAttribute("value");

           assertTrue(codigo != null && !codigo.trim().isEmpty(),
              "O código do pedido não foi gerado.");

         return codigo;
    }


    @Test
    public void deveRealizarPedidoComClienteEProduto() {
           criarPedido();

           WebElement botaoProduto = wait.until(
           ExpectedConditions.elementToBeClickable(
                  By.cssSelector(".bootstrap-select button.dropdown-toggle")));

           botaoProduto.click();

           WebElement opcaoProduto = wait.until(
              ExpectedConditions.elementToBeClickable(
                   By.xpath("//div[contains(@class,'bootstrap-select')]"
                          + "//a[contains(.,'Picolé')]")));

         opcaoProduto.click();

        wait.until(driver ->
              "1".equals(driver.findElement(produtoSelect).getAttribute("value")));
 
        wait.until(ExpectedConditions.elementToBeClickable(inserirProduto)).click();

        wait.until(driver ->
                driver.findElement(tabelaProdutos).getText().contains("Picolé"));

        wait.until(driver ->
                driver.findElement(totalProdutos).getText().contains("6,50"));

        assertTrue(driver.findElement(tabelaProdutos).getText().contains("Picolé"),
                "O produto não foi encontrado na tabela do pedido.");

        assertTrue(driver.findElement(totalProdutos).getText().contains("6,50"),
                "O total do pedido não foi atualizado para R$ 6,50.");
    }

    @Test
    public void deveListarEPesquisarPedidoAberto() {
        String codigo = criarPedido();

        driver.get("http://localhost:8080/venda/status/ABERTA");

        By pedidoNaLista = By.xpath(
                "//a[contains(@class,'aberta')]" +
                "[.//span[contains(normalize-space(.),'Numero: " + codigo + "')]]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(pedidoNaLista));

        WebElement pesquisa = wait.until(
                ExpectedConditions.visibilityOfElementLocated(campoPesquisa));

        pesquisa.sendKeys(codigo);
        pesquisa.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.visibilityOfElementLocated(pedidoNaLista));

        assertTrue(driver.findElement(pedidoNaLista).isDisplayed(),
                "O pedido aberto não foi encontrado após a pesquisa pelo código.");
    }
}

