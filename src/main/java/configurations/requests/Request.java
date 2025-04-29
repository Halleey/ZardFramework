package configurations.requests;

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
        String fullPath = getPath(); // ex: /user/delete/4
        if (!fullPath.startsWith(basePath)) return null;

        // Remove a basePath da URL
        String remaining = fullPath.substring(basePath.length());
        if (remaining.startsWith("/")) {
            remaining = remaining.substring(1);
        }

        // Retorna o primeiro segmento após o basePath
        String[] parts = remaining.split("/");
        return parts.length > 0 ? parts[0] : null;
    }

    // Novo método para pegar parâmetros da query string
    public String getQueryParam(String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }

        String[] params = query.split("&");
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }

        return null;
    }


}