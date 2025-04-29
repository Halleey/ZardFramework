package configurations.routes;

import configurations.Server;
import configurations.handlers.RequestHandler;
import configurations.requests.Request;
import configurations.requests.Response;

import java.lang.reflect.Method;
public class RouterRegister {
    public static void registerRoutes(Server server, Object controller) {
        Method[] methods = controller.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(GetRouter.class)) {
                GetRouter getRoute = method.getAnnotation(GetRouter.class);
                String path = getRoute.value();
                RequestHandler handler = createHandler(controller, method);
                server.get(path, handler);
            }

            if (method.isAnnotationPresent(PostRouter.class)) {
                PostRouter postRoute = method.getAnnotation(PostRouter.class);
                String path = postRoute.value();
                RequestHandler handler = createHandler(controller, method);
                server.post(path, handler);
            }

            if (method.isAnnotationPresent(DeleteRouter.class)) {
                DeleteRouter deleteRouter = method.getAnnotation(DeleteRouter.class);
                String path = deleteRouter.value();
                RequestHandler handler = createHandler(controller, method);
                server.delete(path, handler);
            }

            if (method.isAnnotationPresent(PatchRouter.class)) {
                PatchRouter patchRouter = method.getAnnotation(PatchRouter.class);
                String path = patchRouter.value();
                RequestHandler handler = createHandler(controller, method);
                server.patch(path, handler);
            }
        }
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
