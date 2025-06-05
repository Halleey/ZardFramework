package configurations.security.configcors;

import com.sun.net.httpserver.HttpExchange;
import configurations.core.handlers.RequestInterceptor;

import java.io.IOException;
public class CorsHandler implements RequestInterceptor {

    private final CorsConfiguration config;

    public CorsHandler(CorsConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean intercept(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        // Se for preflight (OPTIONS), responde e finaliza
        if ("OPTIONS".equalsIgnoreCase(method)) {
            if (!config.isMethodAllowed(method)) {
                exchange.getResponseHeaders().add("Allow", config.getAllowedMethodsHeader());
                exchange.sendResponseHeaders(405, -1);
                return true;
            }
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            return true;
        }

        // Para outros métodos, bloqueia se não for permitido
        if (!config.isMethodAllowed(method)) {
            exchange.getResponseHeaders().add("Allow", config.getAllowedMethodsHeader());
            String msg = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, msg.length());
            exchange.getResponseBody().write(msg.getBytes());
            exchange.close();
            return true;
        }

        // Adiciona headers CORS à resposta e segue com a execução normal
        addCorsHeaders(exchange);
        return false; // segue o fluxo normal
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", config.getAllowedOriginsHeader());
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", config.getAllowedMethodsHeader());
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", config.getAllowedHeadersHeader());
    }

    public CorsConfiguration getConfig() {
        return config;
    }
}