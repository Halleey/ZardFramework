package configurations;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import configurations.requests.CorsHandler;
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
    private final CorsHandler corsHandler; // Pode ser null

    public Server(int port, CorsHandler corsHandler) {
        this.port = port;
        this.router = new Router();
        this.corsHandler = corsHandler;
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
            String method = exchange.getRequestMethod();

            // Primeiro trate o preflight OPTIONS SEM bloqueio
            if (corsHandler != null && "OPTIONS".equalsIgnoreCase(method)) {
                if (corsHandler.handlePreflight(exchange)) {
                    return;
                }
            }

            // Agora bloqueie métodos não permitidos para os demais tipos
            if (corsHandler != null && !corsHandler.getConfig().isMethodAllowed(method)) {
                String error = "405 Method Not Allowed";
                exchange.getResponseHeaders().add("Allow", corsHandler.getConfig().getAllowedMethodsHeader());
                exchange.sendResponseHeaders(405, error.length());
                exchange.getResponseBody().write(error.getBytes());
                exchange.close();
                return;
            }

            // Adiciona headers CORS nas respostas normais
            if (corsHandler != null) {
                corsHandler.addCorsHeaders(exchange);
            }

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
