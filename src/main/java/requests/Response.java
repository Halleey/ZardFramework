package requests;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Response {
    private final HttpExchange exchange; // O objeto HttpExchange que representa a resposta HTTP

    public Response(HttpExchange exchange) {
        this.exchange = exchange; // Inicializa com o HttpExchange da resposta
    }

    // Envia uma resposta com o corpo fornecido e código de status 200 (OK)
    public void send(String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8); // Converte o corpo em bytes
        exchange.sendResponseHeaders(200, responseBytes.length); // Define o código de status 200 (OK)
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes); // Escreve os bytes da resposta no corpo da resposta
        }
    }

    // Envia uma resposta com o código de status e corpo fornecido
    public void send(int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8); // Converte o corpo em bytes
        exchange.sendResponseHeaders(statusCode, responseBytes.length); // Define o código de status fornecido
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes); // Escreve os bytes da resposta no corpo da resposta
        }
    }
}