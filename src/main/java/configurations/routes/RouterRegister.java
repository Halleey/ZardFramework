package configurations.routes;

import configurations.Server;
import configurations.handlers.RequestHandler;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.responses.ResponseEntity;
import entities.JsonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class RouterRegister {
    public static void registerRoutes(Server server, Object controller) {
        Class<?> controllerClass = controller.getClass();
        String basePath = getBasePath(controllerClass);

        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GetRouter.class)) {
                GetRouter getRoute = method.getAnnotation(GetRouter.class);
                String path = combinePaths(basePath, getRoute.value());
                server.get(path, createHandler(controller, method));
            }
            if (method.isAnnotationPresent(PostRouter.class)) {
                PostRouter postRoute = method.getAnnotation(PostRouter.class);
                String path = combinePaths(basePath, postRoute.value());
                server.post(path, createHandler(controller, method));
            }
            if (method.isAnnotationPresent(DeleteRouter.class)) {
                DeleteRouter deleteRoute = method.getAnnotation(DeleteRouter.class);
                String path = combinePaths(basePath, deleteRoute.value());
                server.delete(path, createHandler(controller, method));
            }
            if (method.isAnnotationPresent(PatchRouter.class)) {
                PatchRouter patchRoute = method.getAnnotation(PatchRouter.class);
                String path = combinePaths(basePath, patchRoute.value());
                server.patch(path, createHandler(controller, method));
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
                basePath = basePath.substring(0, basePath.length() - 1); // Remove barra final
            }
        }
        return basePath;
    }

    private static String combinePaths(String basePath, String value) {
        if (value == null || value.isEmpty() || value.equals("/")) {
            return basePath;
        }
        // Garante que o caminho do valor sempre comece com "/"
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        return basePath + value;
    }

    //Terminar outra hora suporte dinamico para RequestParam e PathParam
    private static RequestHandler createHandler(Object controller, Method method) {
        return (req, res) -> {
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
                res.send(500, "Erro interno: " + e.getMessage());
            }
        };
    }


    private static Object convertValue(String value, Class<?> type) {
        if (value == null) return null;
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        throw new IllegalArgumentException("Tipo não suportado para query param: " + type);
    }

}
