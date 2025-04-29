package configurations.routes;

import configurations.Server;
import configurations.handlers.RequestHandler;
import configurations.requests.Request;
import configurations.requests.Response;

import java.lang.reflect.Method;

public class RouterRegister {
    public static void registerRoutes(Server server, Object controller) {
        Class<?> controllerClass = controller.getClass();
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


    private static RequestHandler createHandler(Object controller, Method method) {
        return (req, res) -> {
            try {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Object result;

                if (parameterTypes.length == 0) {
                    // Sem parâmetros, apenas invoca
                    result = method.invoke(controller);
                } else if (parameterTypes.length == 2
                        && parameterTypes[0] == Request.class
                        && parameterTypes[1] == Response.class) {
                    // Com Request e Response, invoca passando os objetos
                    method.invoke(controller, req, res);
                    return; // já lidou com a resposta manualmente
                } else {
                    throw new IllegalStateException("Assinatura do método inválida: " + method.getName());
                }

                // Se o método retornar algo, envia como resposta
                if (result != null) {
                    res.send(result.toString());
                } else {
                    res.send(""); // Retorno vazio
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.send(500, "Erro interno no servidor");
            }
        };
    }
}
