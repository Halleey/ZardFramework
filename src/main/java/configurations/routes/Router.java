package configurations.routes;

import configurations.handlers.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private final Map<String, List<Route>> routes = new HashMap<>();

    public void addRoute(String method, String path, RequestHandler handler) {
        path = normalizePath(path);
        System.out.printf("[Router] Registrando rota - Método: %s, Path: %s\n", method, path);
        routes.computeIfAbsent(method, k -> new ArrayList<>()).add(new Route(path, handler));
    }

    public RouteMatch findHandler(String method, String requestPath) {
        normalizePath(requestPath);
       // System.out.printf("[Router] Buscando rota - Método: %s, RequestPath: %s\n", method, requestPath);

        List<Route> methodRoutes = routes.get(method);
        if (methodRoutes == null) {
          //  System.out.printf("[Router] Nenhuma rota registrada para método %s\n", method);
            return null;
        }

        for (Route route : methodRoutes) {
            //System.out.printf("[Router] Tentando casar com rota: %s\n", route.path);
            Map<String, String> pathParams = matchPath(route.path, requestPath); // requestPath vem "cru", sem normalizar
            if (pathParams != null) {
             //   System.out.printf("[Router] Rota casada com sucesso: %s\n", route.path);
              //  System.out.printf("[Router] Params capturados: %s\n", pathParams);
                return new RouteMatch(route.handler, pathParams);
            }
        }
     //   System.out.println("[Router] Nenhuma rota casou com a requisição.");
        return null;
    }

    private Map<String, String> matchPath(String routePath, String requestPath) {
        // IMPORTANTE: aqui normalize APENAS o routePath, não o requestPath
        routePath = normalizePath(routePath);

        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) {
            //System.out.printf("[Router.matchPath] Tamanho diferente: rota=%d, requisição=%d\n", routeParts.length, requestParts.length);
            return null;
        }

        Map<String, String> pathParams = new HashMap<>();
        for (int i = 0; i < routeParts.length; i++) {
            String r = routeParts[i];
            String p = requestParts[i];

            if (r.startsWith("{") && r.endsWith("}")) {
                String key = r.substring(1, r.length() - 1);
                pathParams.put(key, p);
           //     System.out.printf("[Router.matchPath] Capturou path param: %s = %s\n", key, p);
            } else if (!r.equals(p)) {
            //    System.out.printf("[Router.matchPath] Parte diferente na posição %d: esperado='%s', recebido='%s'\n", i, r, p);
                return null;
            }
        }

        return pathParams;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        if (path.length() > 1 && path.endsWith("/")) {
            String normalized = path.substring(0, path.length() - 1);
        //    System.out.printf("[Router.normalizePath] Normalizando path '%s' para '%s'\n", path, normalized);
            return normalized;
        }
        return path;
    }

    private static class Route {
        String path;
        RequestHandler handler;

        Route(String path, RequestHandler handler) {
            this.path = path; // já está normalizado na addRoute
            this.handler = handler;
        }
    }

    public static class RouteMatch {
        public final RequestHandler handler;
        public final Map<String, String> pathParams;

        public RouteMatch(RequestHandler handler, Map<String, String> pathParams) {
            this.handler = handler;
            this.pathParams = pathParams;
        }
    }
}
