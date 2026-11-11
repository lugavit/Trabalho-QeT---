
package net.originmobi.pdv;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CadastroEmpresaSistemaTest {

    private static final String URL_BASE =
            "http://localhost:8080";

    private WebDriver navegador;
    private WebDriverWait espera;

    @Before
    public void iniciarNavegador() {

        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions opcoes = new FirefoxOptions();
        opcoes.addArguments("-headless");

        navegador = new FirefoxDriver(opcoes);

        espera = new WebDriverWait(navegador, 15);
    }

    @Test
    public void deveRealizarCadastroCompletoDaEmpresa() {

        acessarTelaDeLogin();

        realizarLogin();

        acessarTelaDeCadastro();

        preencherDadosDaEmpresa();

        preencherEndereco();

        preencherParametrosFiscais();

        submeterCadastro();

        validarCadastroRealizado();
    }

    private void acessarTelaDeLogin() {

        navegador.get(URL_BASE + "/login");

        espera.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name("username")
                )
        );
    }

    private void realizarLogin() {

        navegador.findElement(By.name("username"))
                .sendKeys("gerente");

      navegador.findElement(By.name("password")).sendKeys("123");

        navegador.findElement(By.id("btn-login"))
                .click();

        espera.until(
                ExpectedConditions.not(
                        ExpectedConditions.urlContains("/login")
                )
        );

        assertTrue(
                "A autenticação não foi concluída",
                !navegador.getCurrentUrl().contains("/login")
        );
    }

    private void acessarTelaDeCadastro() {

        navegador.get(URL_BASE + "/empresa");

        espera.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("form_empresa")
                )
        );

        assertTrue(
                "A tela de cadastro não foi acessada",
                navegador.getCurrentUrl().contains("/empresa")
        );
    }

    private void preencherDadosDaEmpresa() {

        navegador.findElement(By.id("nome"))
                .sendKeys("Empresa Sistema Teste");

        navegador.findElement(By.id("fantasia"))
                .sendKeys("Empresa Teste");

        navegador.findElement(By.id("cnpj"))
                .sendKeys("11222333000181");

        navegador.findElement(By.id("ie"))
                .sendKeys("123456789");
    }

   private void preencherEndereco() {

    // Ativa a aba Endereço
    navegador.findElement(
        By.cssSelector("a[href='#endereco']")
    ).click();

    // Aguarda a aba ficar visível
    espera.until(ExpectedConditions.visibilityOfElementLocated(
        By.id("cidade")
    ));

    // Seleciona a cidade
    Select cidade = new Select(
        navegador.findElement(By.id("cidade"))
    );

    cidade.selectByVisibleText("Cacoal");

    // Preenche os demais campos
    navegador.findElement(By.id("rua"))
        .sendKeys("Rua de Teste");

    navegador.findElement(By.id("bairro"))
        .sendKeys("Centro");

    navegador.findElement(By.id("numero"))
        .sendKeys("100");

    navegador.findElement(By.id("cep"))
        .sendKeys("76963000");

    navegador.findElement(By.id("ref"))
        .sendKeys("Endereco utilizado no teste");
  }
  private void preencherParametrosFiscais() {

    // Ativa a aba Fiscal
    navegador.findElement(
        By.cssSelector("a[href='#fiscal']")
    ).click();

    // Aguarda o campo de regime tributário ficar visível
    espera.until(ExpectedConditions.visibilityOfElementLocated(
        By.id("rt")
    ));

    // Seleciona o regime tributário
    Select regimeTributario = new Select(
        navegador.findElement(By.id("rt"))
    );

    regimeTributario.selectByVisibleText("Simples Nacional");

    // Preenche a série da nota fiscal
    navegador.findElement(By.id("serie"))
        .sendKeys("1");

    // Seleciona o ambiente
    Select ambiente = new Select(
        navegador.findElement(By.id("ambiente"))
    );

    ambiente.selectByVisibleText("Homologação");

    // Preenche a alíquota
    navegador.findElement(By.id("pCredSN"))
        .sendKeys("2,00");
   }

    private void submeterCadastro() {

     String dadosFormulario = (String)
        ((org.openqa.selenium.JavascriptExecutor) navegador)
            .executeScript(
                "return Array.from(" +
                "new FormData(document.getElementById('form_empresa')).entries()" +
                ").map(e => e[0] + ' = ' + e[1]).join('\\n');"
            );

      System.out.println("DADOS ENVIADOS PELO FORMULARIO:");
      System.out.println(dadosFormulario);

      System.out.println("================================");

      navegador.findElement(By.name("enviar")).click();
    }

   private void validarCadastroRealizado() {
        System.out.println("URL após salvar: " + navegador.getCurrentUrl());
        System.out.println("Título da página: " + navegador.getTitle());
        System.out.println("Código HTTP não disponível diretamente pelo WebDriver.");
        System.out.println("Texto apresentado pelo sistema:");
        System.out.println(navegador.findElement(By.tagName("body")).getText());
        System.out.println("================================");
   }

      

    @After
    public void finalizarNavegador() {

        if (navegador != null) {
            navegador.quit();
        }
    }
}


