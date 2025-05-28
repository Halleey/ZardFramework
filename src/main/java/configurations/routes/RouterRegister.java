package configurations.routes;
import configurations.Server;
import configurations.handlers.RequestHandler;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.responses.ResponseEntity;
import configurations.security.AuthFilter;
import configurations.security.EnableSecurity;
import configurations.security.FilterException;
import configurations.security.SecurityFilter;
import configurations.security.auth.SecurityConfig;
import entities.JsonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
public class RouterRegister {

    public static void registerRoutes(Server server, Object controller, SecurityConfig securityConfig) {
        Class<?> controllerClass = controller.getClass();
        String basePath = getBasePath(controllerClass);

        // Garante que as configurações foram aplicadas
        securityConfig.configure();

        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
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
                boolean isPublic = securityConfig.getRouteControl().isPublic(httpMethod, path);
                boolean applySecurity = !isPublic;

                SecurityFilter filter = applySecurity ? securityConfig.getFilterChain() : new SecurityFilter();
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
        String basePath = "";

        if (controllerClass.isAnnotationPresent(RequestController.class)) {
            RequestController annotation = controllerClass.getAnnotation(RequestController.class);
            basePath = annotation.value();
            if (!basePath.startsWith("/")) {
                basePath = "/" + basePath;
            }
            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
        }

        return basePath;
    }

    private static String combinePaths(String basePath, String value) {
        if (value == null || value.isEmpty() || value.equals("/")) {
            return basePath;
        }
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        return basePath + value;
    }

    private static RequestHandler createHandler(Object controller, Method method, boolean applySecurity, SecurityFilter securityFilter) {
        return (req, res) -> {
            try {
                if (applySecurity) {
                    securityFilter.doFilter(req, res);
                }

                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    Class<?> type = parameter.getType();

                    if (type == Request.class) {
                        args[i] = req;
                    } else if (type == Response.class) {
                        args[i] = res;
                    } else if (parameter.isAnnotationPresent(QueryParam.class)) {
                        String key = parameter.getAnnotation(QueryParam.class).value();
                        String rawValue = req.getQueryParam(key);
                        args[i] = convertValue(rawValue, type);
                    } else if (parameter.isAnnotationPresent(PathParam.class)) {
                        String key = parameter.getAnnotation(PathParam.class).value();
                        String rawValue = req.getPathParam(key);
                        args[i] = convertValue(rawValue, type);
                    } else {
                        String body = req.getBody();
                        args[i] = JsonUtils.fromJson(body, type);
                    }
                }

                Object result = method.invoke(controller, args);
                if (result != null) {
                    if (result instanceof ResponseEntity<?> entity) {
                        res.setStatus(entity.getStatusCode());
                        for (Map.Entry<String, String> header : entity.getHeaders().entrySet()) {
                            res.setHeader(header.getKey(), header.getValue());
                        }
                        res.send(entity.getBody());
                    } else {
                        res.send(result.toString());
                    }
                }

            } catch (FilterException e) {
                System.out.println("Bloqueado pelo filtro: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                res.send(500, "Erro interno: " + e.getMessage());
            }
        };
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        // Adicione mais conversões conforme necessário
        return null;
    }
}
