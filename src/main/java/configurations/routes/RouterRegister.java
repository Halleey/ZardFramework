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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class RouterRegister {

    public static void registerRoutes(Server server, Object controller, SecurityConfig securityConfig) {
        Class<?> controllerClass = controller.getClass();
        String basePath = getBasePath(controllerClass);

        if (securityConfig != null) {
            securityConfig.configure();
        }

        for (Method method : controllerClass.getDeclaredMethods()) {
            String httpMethod = null;
            String path = null;

            if (method.isAnnotationPresent(GetRouter.class)) {
                path = combinePaths(basePath, method.getAnnotation(GetRouter.class).value());
                httpMethod = "GET";
            } else if (method.isAnnotationPresent(PostRouter.class)) {
                path = combinePaths(basePath, method.getAnnotation(PostRouter.class).value());
                httpMethod = "POST";
            } else if (method.isAnnotationPresent(DeleteRouter.class)) {
                path = combinePaths(basePath, method.getAnnotation(DeleteRouter.class).value());
                httpMethod = "DELETE";
            } else if (method.isAnnotationPresent(PatchRouter.class)) {
                path = combinePaths(basePath, method.getAnnotation(PatchRouter.class).value());
                httpMethod = "PATCH";
            }

            if (httpMethod != null && path != null) {
                boolean applySecurity = false;
                SecurityFilter filter = new SecurityFilter();

                if (securityConfig != null) {
                    SecurityRouteControl routeControl = securityConfig.getRouteControl();

                    boolean isPublic = routeControl.isPublic(httpMethod, path);
                    boolean requiresRole = routeControl.requiresRole(httpMethod, path);

                    applySecurity = !isPublic || requiresRole;

                    if (applySecurity) {
                        filter = securityConfig.getFilterChain();
                    }
                }

                RequestHandler handler = createHandler(controller, method, applySecurity, filter);

                switch (httpMethod) {
                    case "GET" -> server.get(path, handler);
                    case "POST" -> server.post(path, handler);
                    case "DELETE" -> server.delete(path, handler);
                    case "PATCH" -> server.patch(path, handler);
                    default -> throw new IllegalArgumentException("Método HTTP não suportado: " + httpMethod);
                }
            }
        }
    }

    private static String getBasePath(Class<?> controllerClass) {
        if (!controllerClass.isAnnotationPresent(RequestController.class)) return "";

        String basePath = controllerClass.getAnnotation(RequestController.class).value();
        if (!basePath.startsWith("/")) basePath = "/" + basePath;
        if (basePath.endsWith("/")) basePath = basePath.substring(0, basePath.length() - 1);
        return basePath;
    }

    private static String combinePaths(String basePath, String value) {
        if (value == null || value.isEmpty() || value.equals("/")) return basePath;
        if (!value.startsWith("/")) value = "/" + value;
        return basePath + value;
    }

    private static RequestHandler createHandler(Object controller, Method method, boolean applySecurity, SecurityFilter securityFilter) {
        return (req, res) -> {
            try {
                if (applySecurity) securityFilter.doFilter(req, res);

                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Class<?> type = parameter.getType();

                    if (type == Request.class) args[i] = req;
                    else if (type == Response.class) args[i] = res;
                    else if (parameter.isAnnotationPresent(QueryParam.class)) {
                        String key = parameter.getAnnotation(QueryParam.class).value();
                        args[i] = convertValue(req.getQueryParam(key), type);
                    } else if (parameter.isAnnotationPresent(PathParam.class)) {
                        String key = parameter.getAnnotation(PathParam.class).value();
                        args[i] = convertValue(req.getPathParam(key), type);
                    } else {
                        args[i] = JsonUtils.fromJson(req.getBody(), type);
                    }
                }

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
                System.out.println("Bloqueado pelo filtro: " + e.getMessage());
                res.send(403, "Acesso negado: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                res.send(500, "Erro interno: " + e.getMessage());
            }
        };
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
