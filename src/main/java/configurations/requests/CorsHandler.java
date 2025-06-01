package configurations.requests;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsHandler {

    private final CorsConfiguration config;

    public CorsHandler(CorsConfiguration config) {
        this.config = config;
    }

    /**
     * Adiciona os headers CORS na resposta usando a configuração.
     */
    public void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", config.getAllowedOriginsHeader());
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", config.getAllowedMethodsHeader());
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", config.getAllowedHeadersHeader());
    }

    /**
     * Trata requisição OPTIONS (preflight). Retorna true se for OPTIONS.
     */
    public boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1); // No Content
            exchange.close();
            return true;
        }
        return false;
    }
    public CorsConfiguration getConfig() {
        return config;
    }


}