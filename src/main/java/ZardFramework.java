import configurations.core.Server;
import configurations.core.handlers.RequestInterceptor;
import configurations.orm.EntityManager;
import configurations.security.configcors.CorsConfiguration;
import configurations.security.configcors.CorsHandler;
import configurations.core.routes.RouterRegister;
import configurations.scanners.ZardContext;
import configurations.security.auth.SecurityConfig;

public class ZardFramework {

    public static void run(int port, String basePackage, String entityPackage) throws Exception {
        // Gera o schema automaticamente
        EntityManager.generateSchema(entityPackage);

        // Inicializa o contexto e carrega todos os componentes
        ZardContext context = new ZardContext();
        context.initialize(basePackage);

        // Cria servidor
        Server server = new Server(port);

        // Adiciona automaticamente todos os interceptadores do contexto (inclui CORS, segurança, etc.)
        for (RequestInterceptor interceptor : context.getBeansOfType(RequestInterceptor.class)) {
            server.addInterceptor(interceptor);
        }

        // Recupera a config de segurança apenas para o registro de rotas (não interceptação!)
        SecurityConfig securityConfig = context.getOptional(SecurityConfig.class);

        // Registra rotas automaticamente
        for (Object controller : context.getControllers()) {
            RouterRegister.registerRoutes(server, controller, securityConfig);
        }

        // Inicia o servidor
        server.start();
    }
}
