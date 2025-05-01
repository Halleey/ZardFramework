package configurations.requests;

import com.sun.net.httpserver.HttpExchange;
import configurations.responses.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Response {
    private final HttpExchange exchange;
    private int statusCode = 200; // Valor padrão
    private final Map<String, List<String>> headers = new HashMap<>();
    private boolean headersSent = false;

    public Response(HttpExchange exchange) {
        this.exchange = exchange;
    }

    // Define o código de status manualmente
    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    // Adiciona um header à resposta
    public void setHeader(String key, String value) {
        headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    // Envia a resposta com status atual e corpo fornecido
    public void send(String body) throws IOException {
        if (!headersSent) {
            applyHeaders(); // Aplica os headers antes de enviar
            headersSent = true;
        }
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Envia diretamente com status e corpo, ignorando setStatus()
    public void send(int statusCode, String body) throws IOException {
        setStatus(statusCode);
        send(body);
    }

    // Aplica os headers acumulados no objeto exchange
    private void applyHeaders() {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            exchange.getResponseHeaders().put(entry.getKey(), entry.getValue());
        }
    }
}