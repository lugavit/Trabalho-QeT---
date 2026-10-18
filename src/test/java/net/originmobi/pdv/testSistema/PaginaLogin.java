package net.originmobi.pdv.testSistema;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PaginaLogin {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By usuarioInput = By.cssSelector("input[id='user']");
    private final By senhaInput = By.cssSelector("input[id='password']");
    private final By loginButton = By.cssSelector("button[id='btn-login']");
    private final By erroLogin = By.xpath("//div[contains(@class,'alert-danger')]//span[contains(normalize-space(.),'Usuário ou senha inválidos')]");

    public PaginaLogin(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public void FazerLogin(String usuario, String senha) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usuarioInput)).sendKeys(usuario);
        wait.until(ExpectedConditions.visibilityOfElementLocated(senhaInput)).sendKeys(senha);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    public boolean isErroLoginVisivel() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(erroLogin)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
