package configurations;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import handlers.RequestHandler;
import requests.Request;
import requests.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final Router router;

    public Server(int port) {
        this.port = port;
        this.router = new Router();
    }

    // Metodos para registrar as rota
    public void get(String path, RequestHandler handler) {
        router.addRoute("GET", path, handler);
    }

    public void post(String path, RequestHandler handler) {
        router.addRoute("POST", path, handler);
    }

    // Inicia o servidor HTTP na porta configurada
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0); // Cria o servidor HTTP na porta definida
        server.createContext("/", this::handleRequest); // Define o manipulador de requisições para todas as requisições (caminho raiz "/")
        server.setExecutor(Executors.newFixedThreadPool(10)); // Configura um pool de threads para lidar com requisições de forma concorrente
        server.start(); // Inicia o servidor
        System.out.println("Servidor rodando na porta " + port);
    }

    // Método responsável por tratar as requisições recebidas
    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod(); // Obtém o método HTTP da requisição
        String path = exchange.getRequestURI().getPath(); // Obtém a URL solicitada
        RequestHandler handler = router.findHandler(method, path); // Procura o handler para o método e caminho

        if (handler != null) {
            // Se o handler for encontrado, cria os objetos Request e Response e chama o handler
            Request req = new Request(exchange);
            Response res = new Response(exchange);
            handler.handle(req, res); // Chama o método handle do handler com a requisição e resposta
        } else {
            // Se o handler não for encontrado, retorna um erro 404
            String notFound = "404 Not Found";
            exchange.sendResponseHeaders(404, notFound.length());
            exchange.getResponseBody().write(notFound.getBytes());
            exchange.close();
        }
    }
}