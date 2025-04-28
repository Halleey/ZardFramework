package configurations.routes;

import configurations.Server;
import configurations.handlers.RequestHandler;

import java.lang.reflect.Method;

public class RouterRegister {
    // Registra todas as rotas de uma classe com base nas anotações
    public static void registerRoutes(Server server, Object controller) {
        // Obter todos os métodos da classe controller
        Method[] methods = controller.getClass().getDeclaredMethods();

        for (Method method : methods) {
            // Verificar se o método tem a anotação @GetRoute
            if (method.isAnnotationPresent(GetRouter.class)) {
                GetRouter getRoute = method.getAnnotation(GetRouter.class);
                String path = getRoute.value();
                RequestHandler handler = (req, res) -> {
                    try {
                        method.invoke(controller, req, res); // Invoca o método no controller
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                server.get(path, handler); // Registrar rota GET
            }

            // Verificar se o método tem a anotação @PostRoute
            if (method.isAnnotationPresent(PostRouter.class)) {
                PostRouter postRoute = method.getAnnotation(PostRouter.class);
                String path = postRoute.value();
                RequestHandler handler = (req, res) -> {
                    try {
                        method.invoke(controller, req, res); // Invoca o método no controller
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                server.post(path, handler); // Registrar rota POST
            }

            if (method.isAnnotationPresent(DeleteRouter.class)) {
                DeleteRouter deleteRouter = method.getAnnotation(DeleteRouter.class);
                String path = deleteRouter.value();
                RequestHandler handler = (req, res) -> {
                    try {
                        method.invoke(controller, req, res);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                server.delete(path, handler);
            }
            if(method.isAnnotationPresent(PatchRouter.class)) {
                PatchRouter patchRouter = method.getAnnotation(PatchRouter.class);
                String path =  patchRouter.value();
                RequestHandler handler = (req, res) -> {
                    try {
                        method.invoke(controller, req, res);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                };
                server.patch(path, handler);
            }
        }
    }
}
