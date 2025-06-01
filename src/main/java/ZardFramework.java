import configurations.Server;
import configurations.orm.EntityManager;
import configurations.requests.CorsConfiguration;
import configurations.requests.CorsHandler;
import configurations.routes.RouterRegister;
import configurations.scanners.ZardContext;
import configurations.security.auth.SecurityConfig;
public class ZardFramework {

    public static void run(int port, String basePackage, String entityPackage) throws Exception {
        // Gera o schema automaticamente
        EntityManager.generateSchema(entityPackage);

        // Inicializa o contexto e carrega todos os componentes
        ZardContext context = new ZardContext();
        context.initialize(basePackage);

        // Recupera configuração de segurança, se houver
        SecurityConfig securityConfig = null;
        try {
            securityConfig = context.get(SecurityConfig.class);
        } catch (Exception ignored) {
            System.out.println("Segurança desabilitada: nenhuma configuração detectada.");
        }

        // Recupera configuração de CORS, se houver
        CorsConfiguration corsConfig = null;
        try {
            corsConfig = context.get(CorsConfiguration.class);
        } catch (Exception ignored) {
            System.out.println("CORS desabilitado: nenhuma configuração detectada.");
        }

        // Cria handler de CORS (pode ser null)
        CorsHandler corsHandler = corsConfig != null ? new CorsHandler(corsConfig) : null;

        // Inicializa o servidor com CORS (se disponível)
        Server server = new Server(port, corsHandler);

        // Registra rotas
        for (Object controller : context.getControllers()) {
            RouterRegister.registerRoutes(server, controller, securityConfig);
        }

        // Inicia o servidor
        server.start();
    }
}
