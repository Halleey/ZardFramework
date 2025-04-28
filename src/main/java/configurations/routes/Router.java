package configurations.routes;

import configurations.handlers.RequestHandler;

import java.util.HashMap;
import java.util.Map;

public class Router {
    private final Map<String, RequestHandler> getRoutes = new HashMap<>();
    private final Map<String, RequestHandler> postRoutes = new HashMap<>();
    private final Map<String, RequestHandler> deleteRoutes = new HashMap<>();
    private final Map<String, RequestHandler> patchRoutes = new HashMap<>();



    // Adiciona uma rota GET
    public void addRoute(String method, String path, RequestHandler handler) {

        switch (method)  {
            case "GET" -> {
                getRoutes.put(path, handler);
            }
            case "POST" -> {
                postRoutes.put(path, handler);
            }
            case "DELETE" -> {
                deleteRoutes.put(path, handler);
            }
            case "PATCH" -> {
                patchRoutes.put(path, handler);
            }
        }
    }

    // Encontra o handler para a rota especificada
    public RequestHandler findHandler(String method, String path) {
        Map<String, RequestHandler> routes;
        switch (method) {
            case "GET" -> routes = getRoutes;
            case "POST" -> routes = postRoutes;
            case "DELETE" -> routes = deleteRoutes;
            case "PATCH" -> routes = patchRoutes;
            default -> {
                return null;
            }
        }
        // Primeiro, tenta encontrar rota exata
        RequestHandler handler = routes.get(path);
        if (handler != null) {
            return handler;
        }

        // Se não achar, tenta encontrar rota que é prefixo (ex: "/delete" para "/delete/1")
        for (Map.Entry<String, RequestHandler> entry : routes.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}