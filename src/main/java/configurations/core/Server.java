package configurations.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import configurations.core.handlers.RequestInterceptor;
import configurations.core.routes.Router;
import configurations.core.handlers.RequestHandler;
import configurations.core.requests.Request;
import configurations.core.requests.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final Router router;
    private final List<RequestInterceptor> interceptors = new ArrayList<>();

    public Server(int port) {
        this.port = port;
        this.router = new Router();
    }

    // Permite registrar interceptors (como CORS, Auth, etc.)
    public void addInterceptor(RequestInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    // Métodos para registrar rotas
    public void get(String path, RequestHandler handler) {
        router.addRoute("GET", path, handler);
    }

    public void post(String path, RequestHandler handler) {
        router.addRoute("POST", path, handler);
    }

    public void patch(String path, RequestHandler handler) {
        router.addRoute("PATCH", path, handler);
    }

    public void delete(String path, RequestHandler handler) {
        router.addRoute("DELETE", path, handler);
    }

    // Inicia o servidor HTTP
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleRequest);
        server.setExecutor(Executors.newFixedThreadPool(20));
        server.start();
        System.out.println("Servidor rodando na porta " + port);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try {
            // Executa todos os interceptors registrados
            for (RequestInterceptor interceptor : interceptors) {
                boolean handled = interceptor.intercept(exchange);
                if (handled) {
                    return; // Interceptor já tratou a requisição
                }
            }

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            Router.RouteMatch match = router.findHandler(method, path);

            if (match != null) {
                Request req = new Request(exchange);
                req.setPathParams(match.pathParams);
                Response res = new Response(exchange);
                match.handler.handle(req, res);
            } else {
                String notFound = "404 Not Found";
                System.out.println("Thread: " + Thread.currentThread().getName() + " — rota não encontrada: " + path);
                exchange.sendResponseHeaders(404, notFound.length());
                exchange.getResponseBody().write(notFound.getBytes());
                exchange.close();
            }

        } catch (Exception e) {
            String error = "500 Internal Server Error";
            System.err.println("Erro ao processar requisição: " + e.getMessage());
            e.printStackTrace();
            exchange.sendResponseHeaders(500, error.length());
            exchange.getResponseBody().write(error.getBytes());
            exchange.close();
        }
    }
}