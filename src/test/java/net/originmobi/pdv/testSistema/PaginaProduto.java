package net.originmobi.pdv.testSistema;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    private final By mensagemSucesso = By.cssSelector("div.alert-success");
    private final By tabelaProdutos = By.cssSelector("table tbody tr");

    public PaginaProduto(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void CriarNovoProduto(String Descricao, String custo, String venda, String unidade, String validade) {
        wait.until(ExpectedConditions.elementToBeClickable(botaoNovoProduto)).click();

        wait.until(ExpectedConditions.elementToBeClickable(descricaoInput)).sendKeys(Descricao);
        wait.until(ExpectedConditions.elementToBeClickable(custoInput)).sendKeys(custo);
        wait.until(ExpectedConditions.elementToBeClickable(vendaInput)).sendKeys(venda);
        wait.until(ExpectedConditions.elementToBeClickable(unidadeInput)).sendKeys(unidade);
        wait.until(ExpectedConditions.elementToBeClickable(validadeInput)).sendKeys(validade);

        WebElement dropdownElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[id='st']")));

        Select dropdown = new Select(dropdownElement);
        dropdown.selectByValue("NAO");

        wait.until(ExpectedConditions.elementToBeClickable(botaoSalvar)).click();
    }

    public boolean verificarMensagemSucesso() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(mensagemSucesso));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean verificarProdutoNaLista(String descricao) {
        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tabelaProdutos));
            String xpath = "//table//tbody//tr//td[contains(text(), '" + descricao + "')]";
            return driver.findElements(By.xpath(xpath)).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
}
