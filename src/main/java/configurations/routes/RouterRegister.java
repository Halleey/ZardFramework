package configurations.routes;
import configurations.Server;
import configurations.handlers.RequestHandler;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.responses.ResponseEntity;
import configurations.security.FilterException;
import configurations.security.SecurityFilter;
import configurations.security.auth.SecurityConfig;
import configurations.security.auth.SecurityRouteControl;
import project.entities.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
public class RouterRegister {

    public static void registerRoutes(Server server, Object controller, SecurityConfig securityConfig) {
        Class<?> controllerClass = controller.getClass();
        String basePath = getBasePath(controllerClass);

        if (securityConfig != null) {
            securityConfig.configure(); // Inicializa qualquer configuração
        }

        for (Method method : controllerClass.getDeclaredMethods()) {
            String httpMethod = getHttpMethod(method);
            String routePath = getRoutePath(basePath, method);

            if (httpMethod != null && routePath != null) {
                boolean applySecurity = false;
                SecurityFilter filter = new SecurityFilter();

                if (securityConfig != null) {
                    SecurityRouteControl control = securityConfig.getRouteControl();
                    boolean isPublic = control.isPublic(httpMethod, routePath);
                    boolean needsRole = control.requiresRole(httpMethod, routePath);

                    applySecurity = !isPublic || needsRole;

                    if (applySecurity) {
                        filter = securityConfig.getFilterChain();
                    }
                }

                RequestHandler handler = createHandler(controller, method, applySecurity, filter);
                registerRoute(server, httpMethod, routePath, handler);
            }
        }
    }

    private static String getBasePath(Class<?> controllerClass) {
        if (!controllerClass.isAnnotationPresent(RequestController.class)) return "";
        String base = controllerClass.getAnnotation(RequestController.class).value();
        if (!base.startsWith("/")) base = "/" + base;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base;
    }

    private static String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetRouter.class)) return "GET";
        if (method.isAnnotationPresent(PostRouter.class)) return "POST";
        if (method.isAnnotationPresent(DeleteRouter.class)) return "DELETE";
        if (method.isAnnotationPresent(PatchRouter.class)) return "PATCH";
        return null;
    }

    private static String getRoutePath(String basePath, Method method) {
        String subPath = null;
        if (method.isAnnotationPresent(GetRouter.class)) subPath = method.getAnnotation(GetRouter.class).value();
        else if (method.isAnnotationPresent(PostRouter.class)) subPath = method.getAnnotation(PostRouter.class).value();
        else if (method.isAnnotationPresent(DeleteRouter.class)) subPath = method.getAnnotation(DeleteRouter.class).value();
        else if (method.isAnnotationPresent(PatchRouter.class)) subPath = method.getAnnotation(PatchRouter.class).value();

        if (subPath == null || subPath.isEmpty() || subPath.equals("/")) return basePath;
        if (!subPath.startsWith("/")) subPath = "/" + subPath;
        return basePath + subPath;
    }

    private static void registerRoute(Server server, String method, String path, RequestHandler handler) {
        switch (method) {
            case "GET" -> server.get(path, handler);
            case "POST" -> server.post(path, handler);
            case "DELETE" -> server.delete(path, handler);
            case "PATCH" -> server.patch(path, handler);
            default -> throw new IllegalArgumentException("Método HTTP não suportado: " + method);
        }
    }

    private static RequestHandler createHandler(Object controller, Method method, boolean applySecurity, SecurityFilter securityFilter) {
        return (req, res) -> {
            try {
                if (applySecurity) {
                    securityFilter.doFilter(req, res);
                }

                Object[] args = resolveMethodArgs(method, req, res);
                Object result = method.invoke(controller, args);

                if (result != null) {
                    if (result instanceof ResponseEntity<?> entity) {
                        res.setStatus(entity.getStatusCode());
                        entity.getHeaders().forEach(res::setHeader);
                        res.send(entity.getBody());
                    } else {
                        res.send(result);
                    }
                }

            } catch (FilterException e) {
                System.out.println("Acesso bloqueado pelo filtro: " + e.getMessage());
                res.send(403, "Acesso negado: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                res.send(500, "Erro interno: " + e.getMessage());
            }
        };
    }

    private static Object[] resolveMethodArgs(Method method, Request req, Response res) throws IOException {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            if (type == Request.class) {
                args[i] = req;
            } else if (type == Response.class) {
                args[i] = res;
            } else if (param.isAnnotationPresent(QueryParam.class)) {
                String key = param.getAnnotation(QueryParam.class).value();
                args[i] = convertValue(req.getQueryParam(key), type);
            } else if (param.isAnnotationPresent(PathParam.class)) {
                String key = param.getAnnotation(PathParam.class).value();
                args[i] = convertValue(req.getPathParam(key), type);
            } else {
                args[i] = JsonUtils.fromJson(req.getBody(), type);
            }
        }

        return args;
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null) return null;
        return switch (targetType.getSimpleName()) {
            case "String" -> value;
            case "int", "Integer" -> Integer.parseInt(value);
            case "long", "Long" -> Long.parseLong(value);
            case "double", "Double" -> Double.parseDouble(value);
            case "boolean", "Boolean" -> Boolean.parseBoolean(value);
            default -> null;
        };
    }
}
