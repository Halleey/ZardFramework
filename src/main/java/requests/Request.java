package requests;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Request {
    private final HttpExchange exchange; // O objeto HttpExchange que representa a requisição HTTP

    public Request(HttpExchange exchange) {
        this.exchange = exchange; // Inicializa com o HttpExchange da requisição
    }

    // Obtém o método HTTP da requisição (GET, POST, etc.)
    public String getMethod() {
        return exchange.getRequestMethod();
    }

    // Obtém o caminho da URL da requisição
    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    // Obtém o corpo da requisição (conteúdo enviado pelo cliente)
    public String getBody() throws IOException {
        InputStream inputStream = exchange.getRequestBody(); // Obtém o InputStream do corpo da requisição
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8); // Lê os bytes e os converte em string
    }

    public String extractPathParam(String basePath) {
        String path = getPath();
        if (path.startsWith(basePath)) {
            String[] parts = path.split("/");
            if (parts.length > 2) {
                return parts[2]; // Pega o valor após /delete/
            }
        }
        return null;
    }


}