package configurations;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import configurations.routes.Router;
import configurations.handlers.RequestHandler;
import configurations.requests.Request;
import configurations.requests.Response;

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

    public void patch(String path, RequestHandler handler) {
        router.addRoute("PATCH", path, handler);
    }
    public void delete(String path, RequestHandler handler) {
        router.addRoute("DELETE", path, handler);
    }

    public void post(String path, RequestHandler handler) {
        router.addRoute("POST", path, handler);
    }

    // Inicia o servidor HTTP na porta configurada
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0); // Cria o servidor HTTP na porta definida
        server.createContext("/", this::handleRequest); // Define o manipulador de requisições para todas as requisições (caminho raiz "/")
        server.setExecutor(Executors.newFixedThreadPool(20)); // Configura um pool de threads para lidar com requisições de forma concorrente
        server.start(); // Inicia o servidor
        System.out.println("Servidor rodando na porta " + port);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Router.RouteMatch match = router.findHandler(method, path);

        if (match != null) {
            Request req = new Request(exchange);
            req.setPathParams(match.pathParams); // importante!
            Response res = new Response(exchange);
            match.handler.handle(req, res);
        } else {
            String notFound = "404 Not Found";
            System.out.println("Thread: " + Thread.currentThread().getName() +
                    " não encontrou rota para: " + path);
            exchange.sendResponseHeaders(404, notFound.length());
            exchange.getResponseBody().write(notFound.getBytes());
            exchange.close();
        }
    }

}