package net.originmobi.pdv.testSistema;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class PaginaPrincipal {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By menuLateral = By.cssSelector(".left");
    private final By botaoProdutosAbrir = By.xpath(
            "//a[.//img[contains(@src, '/icons/menu/produto-menu.png')]]");
    private final By botaoProdutosProduto = By.cssSelector("a[href='/produto']");

    public PaginaPrincipal(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void clicarBotaoProdutoProduto() {
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(menuLateral));
        new Actions(driver).moveToElement(menu).perform();
        wait.until(webDriver -> webDriver.findElement(menuLateral).getSize().getWidth() > 200);

        wait.until(ExpectedConditions.elementToBeClickable(botaoProdutosAbrir)).click();
        wait.until(ExpectedConditions.elementToBeClickable(botaoProdutosProduto)).click();
    }
}
