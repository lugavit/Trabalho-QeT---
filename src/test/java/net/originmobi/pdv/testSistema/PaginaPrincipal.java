package net.originmobi.pdv.testSistema;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class PaginaPrincipal {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By botaoInicio = By.cssSelector("a:has(img[src*='/icons/menu/logo.png'])");
    private final By botaoPedidos = By.cssSelector("a:has(img[src*='/icons/menu/pedido-menu.png'])");
    private final By botaoCaixaCofre = By.cssSelector("a:has(img[src*='/icons/menu/caixa-menu.png'])");
    private final By botaoBanco = By.cssSelector("a:has(img[src*='/icons/menu/banco-menu.png'])");
    private final By botaoReceber = By.cssSelector("a:has(img[src*='/icons/menu/receber.png'])");
    private final By botaoDespesas = By.cssSelector("a:has(img[src*='/icons/menu/pagar.png'])");
    private final By botaoGerenciaCartoes = By.cssSelector("a:has(img[src*='/icons/menu/cartoes-menu.png'])");
    private final By botaoPessoas = By.cssSelector("a:has(img[src*='/icons/menu/pessoa-menu.png'])");
    private final By botaoFornecedor = By.cssSelector("a:has(img[src*='/icons/menu/fornecedor-menu.png'])");
    private final By botaoFormaPagamento = By.cssSelector("a:has(img[src*='/icons/menu/forma-pagamento-menu.png'])");
    private final By botaoNotaFiscal = By.cssSelector("a:has(img[src*='/icons/menu/nfe.png'])");
    private final By botaoGerenciarUsuarios = By.cssSelector("a:has(img[src*='/icons/menu/usuario-menu.png'])");

    private final By botaoProdutosAbrir = By.cssSelector("a:has(img[src*='/icons/menu/produto-menu.png'])");
    private final By botaoProdutosProduto = By.cssSelector("a[href='/produto']");
    private final By botaoProdutosGrupo = By.cssSelector("a[href='/grupo']");
    private final By botaoProdutosCategoria = By.cssSelector("a[href='/categoria']");
    private final By botaoProdutosTributacao = By.cssSelector("a[href='/tributacao']");
    private final By botaoProdutosAjusteEstoque = By.cssSelector("a[href='/ajustes']");


    public PaginaPrincipal(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void clicarBotaoProdutoProduto() {
        Actions actions = new Actions(driver);
        actions.moveToElement(wait.until(ExpectedConditions.elementToBeClickable(botaoProdutosAbrir))).click().perform();
        wait.until(ExpectedConditions.elementToBeClickable(botaoProdutosProduto));
        actions.moveToElement(driver.findElement(botaoProdutosProduto)).click().perform();
    }


    
}
