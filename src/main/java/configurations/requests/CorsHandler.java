package configurations.requests;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsHandler {

    public static final String ALLOWED_ORIGIN = "*";
    public static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, PATCH, OPTIONS";
    public static final String ALLOWED_HEADERS = "Content-Type, Authorization";

    /**
     * Adiciona os headers CORS básicos na resposta.
     */
    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", ALLOWED_METHODS);
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", ALLOWED_HEADERS);
    }

    /**
     * Trata a requisição OPTIONS (preflight).
     * Retorna true se a requisição foi OPTIONS e já tratada.
     */
    public static boolean handlePreflight(HttpExchange exchange) throws IOException, IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1); // No Content
            exchange.close();
            return true;
        }
        return false;
    }
}
