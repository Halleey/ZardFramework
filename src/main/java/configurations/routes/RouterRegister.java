package configurations.routes;
import configurations.Server;
import configurations.handlers.RequestHandler;
import configurations.parsers.MultiFile;
import configurations.parsers.MultipartFile;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.responses.ResponseEntity;
import configurations.security.FilterException;
import configurations.security.SecurityFilter;
import configurations.security.auth.SecurityConfig;
import configurations.security.auth.SecurityRouteControl;
import project.entities.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.Map;

public class RouterRegister {

    public static void registerRoutes(Server server, Object controller, SecurityConfig securityConfig) {
        Class<?> controllerClass = controller.getClass();
        String basePath = getBasePath(controllerClass);

        if (securityConfig != null) {
            securityConfig.configure(); // Inicializa configurações
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

        MapHolder holder = new MapHolder();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();
            args[i] = resolveArgument(param, type, req, res, holder);
        }

        return args;
    }
    private static Object resolveArgument(Parameter param, Class<?> type, Request req, Response res, MapHolder holder) throws IOException {
        if (type == Request.class) return req;
        if (type == Response.class) return res;

        if (param.isAnnotationPresent(QueryParam.class)) {
            String key = param.getAnnotation(QueryParam.class).value();
            return convertValue(req.getQueryParam(key), type);
        }

        if (param.isAnnotationPresent(PathParam.class)) {
            String key = param.getAnnotation(PathParam.class).value();
            return convertValue(req.getPathParam(key), type);
        }

        if (param.isAnnotationPresent(MultiFile.class)) {
            ensureMultipartParsed(req, holder);
            String fieldName = param.getAnnotation(MultiFile.class).value();
            return holder.files.get(fieldName);
        }

        // DTOs ou entidades
        if (isMultipartForm(req) && !type.isPrimitive() && !type.equals(String.class)) {
            ensureMultipartParsed(req, holder);
            return mapMultipartFieldsToObject(holder.fields, type);
        }

        // JSON fallback
        return JsonUtils.fromJson(req.getBody(), type);
    }

    private static void parseMultipart(Request req) throws IOException {
        req.getFormFields(); // já faz caching internamente
        req.getFormFiles();
    }

    private static void ensureMultipartParsed(Request req, MapHolder holder) throws IOException {
        if (!holder.parsed) {
            parseMultipart(req);
            holder.fields = req.getFormFields();
            holder.files = req.getFormFiles();
            holder.parsed = true;
        }
    }

    private static class MapHolder {
        boolean parsed = false;
        Map<String, String> fields;
        Map<String, MultipartFile> files;
    }


    private static boolean isMultipartForm(Request req) {
        String ct = req.getContentType();
        return ct != null && ct.startsWith("multipart/form-data");
    }


    private static <T> T mapMultipartFieldsToObject(Map<String, String> fields, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String value = fields.get(field.getName());
                if (value != null) {
                    Object converted = convertValue(value, field.getType());
                    field.set(obj, converted);
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao mapear multipart fields para " + clazz.getSimpleName(), e);
        }
    }

    private static Object convertValue(String value, Class<?> targetType) {
        if (value == null) return null;

        return switch (targetType.getSimpleName()) {
            case "String" -> value;
            case "int", "Integer" -> Integer.parseInt(value);
            case "long", "Long" -> Long.parseLong(value);
            case "double", "Double" -> Double.parseDouble(value);
            case "boolean", "Boolean" -> Boolean.parseBoolean(value);
            case "BigDecimal" -> new BigDecimal(value);
            default -> null;
        };
    }
}
