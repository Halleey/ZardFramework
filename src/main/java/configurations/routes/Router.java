package configurations.routes;

import configurations.handlers.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private final Map<String, List<Route>> routes = new HashMap<>();

    public void addRoute(String method, String path, RequestHandler handler) {
        routes.computeIfAbsent(method, k -> new ArrayList<>()).add(new Route(path, handler));
    }

    public RouteMatch findHandler(String method, String requestPath) {
        List<Route> methodRoutes = routes.get(method);
        if (methodRoutes == null) return null;

        for (Route route : methodRoutes) {
            Map<String, String> pathParams = matchPath(route.path, requestPath);
            if (pathParams != null) {
                return new RouteMatch(route.handler, pathParams);
            }
        }

        return null;
    }

    private Map<String, String> matchPath(String routePath, String requestPath) {
        String[] routeParts = routePath.split("/");
        String[] requestParts = requestPath.split("/");

        if (routeParts.length != requestParts.length) return null;

        Map<String, String> pathParams = new HashMap<>();
        for (int i = 0; i < routeParts.length; i++) {
            String r = routeParts[i];
            String p = requestParts[i];

            if (r.startsWith("{") && r.endsWith("}")) {
                String key = r.substring(1, r.length() - 1);
                pathParams.put(key, p);
            } else if (!r.equals(p)) {
                return null;
            }
        }

        return pathParams;
    }

    private static class Route {
        String path;
        RequestHandler handler;

        Route(String path, RequestHandler handler) {
            this.path = path;
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
