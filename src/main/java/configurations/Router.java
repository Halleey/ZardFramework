package configurations;

import handlers.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private final Map<String, RequestHandler> getRoutes = new HashMap<>();
    private final Map<String, RequestHandler> postRoutes = new HashMap<>();

    // Adiciona uma rota GET
    public void addRoute(String method, String path, RequestHandler handler) {
        if ("GET".equals(method)) {
            getRoutes.put(path, handler);
        } else if ("POST".equals(method)) {
            postRoutes.put(path, handler);
        }
    }

    // Encontra o handler para a rota especificada
    public RequestHandler findHandler(String method, String path) {
        if ("GET".equals(method)) {
            return getRoutes.get(path);
        } else if ("POST".equals(method)) {
            return postRoutes.get(path);
        }
        return null;
    }
}