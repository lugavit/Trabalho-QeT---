package net.originmobi.pdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class HttpEndpointIntegrationTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Test
    public void loginAllowsAccessToProductsEndpoint() throws Exception {
        HttpURLConnection loginConnection = (HttpURLConnection) new URL(BASE_URL + "/login").openConnection();
        loginConnection.setRequestMethod("POST");
        loginConnection.setConnectTimeout(5000);
        loginConnection.setReadTimeout(5000);
        loginConnection.setInstanceFollowRedirects(false);
        loginConnection.setDoOutput(true);
        loginConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        String credentials = "username=" + URLEncoder.encode("gerente", "UTF-8")
                + "&password=" + URLEncoder.encode("123", "UTF-8");
        try (OutputStream requestBody = loginConnection.getOutputStream()) {
            requestBody.write(credentials.getBytes(StandardCharsets.UTF_8));
        }

        try {
            int loginStatus = loginConnection.getResponseCode();
            assertEquals("O login deve redirecionar após autenticar", HttpURLConnection.HTTP_MOVED_TEMP, loginStatus);
            assertEquals("/", new URL(loginConnection.getHeaderField("Location")).getPath());

            String setCookie = loginConnection.getHeaderField("Set-Cookie");
            assertNotNull("A resposta de login deve criar uma sessão", setCookie);
            String sessionCookie = setCookie.split(";", 2)[0];

            HttpURLConnection productsConnection = (HttpURLConnection) new URL(BASE_URL + "/produto").openConnection();
            productsConnection.setRequestMethod("GET");
            productsConnection.setConnectTimeout(5000);
            productsConnection.setReadTimeout(5000);
            productsConnection.setRequestProperty("Cookie", sessionCookie);
            try {
                int productsStatus = productsConnection.getResponseCode();
                assertEquals("A sessão autenticada deve acessar a lista de produtos",
                        HttpURLConnection.HTTP_OK, productsStatus);
                assertTrue("A resposta deve conter a página de produtos",
                        readResponse(productsConnection.getInputStream()).contains("Produto"));
            } finally {
                productsConnection.disconnect();
            }
        } finally {
            loginConnection.disconnect();
        }
    }

    private String readResponse(InputStream input) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
}
