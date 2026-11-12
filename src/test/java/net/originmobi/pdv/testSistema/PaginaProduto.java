package net.originmobi.pdv.testSistema;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;

public class PaginaProduto {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By botaoNovoProduto = By.cssSelector("a[href='/produto/form']");

    private final By descricaoInput = By.cssSelector("input[id='descricao']");
    private final By custoInput = By.cssSelector("input[id='valorCusto']");
    private final By vendaInput = By.cssSelector("input[id='valorVenda']");
    private final By unidadeInput = By.cssSelector("input[id='unidade']");
    private final By validadeInput = By.cssSelector("input[id='validade']");

    private final By botaoSalvar = By.cssSelector("input[name='enviar']");
    private final By situacaoSelect = By.id("st");
    private final By mensagemSucesso = By.cssSelector("div.alert-success");
    private final By tabelaProdutos = By.cssSelector("table tbody tr");

    public PaginaProduto(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void CriarNovoProduto(String descricao, String custo, String venda, String unidade, String validade) {
        wait.until(ExpectedConditions.elementToBeClickable(botaoNovoProduto)).click();

        wait.until(ExpectedConditions.elementToBeClickable(descricaoInput)).sendKeys(descricao);
        wait.until(ExpectedConditions.elementToBeClickable(custoInput)).sendKeys(custo);
        wait.until(ExpectedConditions.elementToBeClickable(vendaInput)).sendKeys(venda);
        wait.until(ExpectedConditions.elementToBeClickable(unidadeInput)).sendKeys(unidade);
        wait.until(ExpectedConditions.elementToBeClickable(validadeInput)).sendKeys(validade);

        WebElement situacaoElement = wait.until(
                ExpectedConditions.elementToBeClickable(situacaoSelect));
        new Select(situacaoElement).selectByValue("NAO");

        wait.until(ExpectedConditions.elementToBeClickable(botaoSalvar)).click();
    }

    public boolean verificarMensagemSucesso() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(mensagemSucesso));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean verificarProdutoNaLista(String descricao) {
        try {
            return wait.until(ignored -> driver.findElements(tabelaProdutos).stream()
                    .anyMatch(linha -> linha.getText().contains(descricao)));
        } catch (TimeoutException e) {
            return false;
        }
    }
}
