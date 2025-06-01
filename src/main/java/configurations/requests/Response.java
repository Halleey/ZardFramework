package configurations.requests;

import com.sun.net.httpserver.HttpExchange;
import project.entities.JsonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Response {
    private final HttpExchange exchange;
    private int statusCode = 200;
    private final Map<String, List<String>> headers = new HashMap<>();
    private boolean headersSent = false;

    public Response(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setHeader(String key, String value) {
        headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void send(String body) throws IOException {
        if (!headersSent) {
            applyHeaders();
            headersSent = true;
        }
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public void sendStatus(int statusCode) throws IOException {
        setStatus(statusCode);
        exchange.sendResponseHeaders(statusCode, -1);
        close();
    }

    public void close() throws IOException {
        exchange.close();
    }

    public void send(Object body) throws IOException {
        if (body instanceof String str) {
            System.out.println("[DEBUG] Enviando String");
            send(str);
        } else if (body instanceof byte[] bytes) {
            System.out.printf("[DEBUG] Enviando byte[]. Tamanho: %d bytes%n", bytes.length);
            if (!headersSent) {
                applyHeaders();
                headersSent = true;
            }

            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else if (body instanceof InputStream stream) {
            System.out.println("[DEBUG] Enviando InputStream");

            if (!headersSent) {
                applyHeaders();
                headersSent = true;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            stream.transferTo(buffer);
            byte[] bytes = buffer.toByteArray();

            System.out.printf("[DEBUG] InputStream convertido em byte[]. Tamanho: %d bytes%n", bytes.length);

            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            System.out.println("[DEBUG] Enviando objeto como JSON: " + body.getClass().getSimpleName());

            String responseBody = JsonUtils.toJson(body);
            setHeader("Content-Type", "application/json");
            send(responseBody);
        }
    }


    public void send(int statusCode, String body) throws IOException {
        setStatus(statusCode);
        send(body);
    }

    private void applyHeaders() {
        headers.forEach((key, values) -> exchange.getResponseHeaders().put(key, values));
    }
}
