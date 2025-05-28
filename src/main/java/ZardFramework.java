import configurations.Server;
import configurations.orm.EntityManager;
import configurations.routes.RouterRegister;
import configurations.scanners.ZardContext;
import configurations.security.auth.SecurityConfig;

public class ZardFramework {

    public static void run(int port, String basePackage, String entityPackage) throws Exception {
        // Inicializa o servidor
        Server server = new Server(port);

        // Gera o schema automaticamente
        EntityManager.generateSchema(entityPackage);

        // Inicializa o contexto e carrega todos os componentes
        ZardContext context = new ZardContext();
        context.initialize(basePackage);

        // Tenta carregar configuração de segurança, se existir
        SecurityConfig securityConfig = null;
        try {
            securityConfig = context.get(SecurityConfig.class);
        } catch (Exception ignored) {
            System.out.println("🔓 Segurança desabilitada: nenhuma configuração detectada.");
        }

        // Registra automaticamente todos os controladores e suas rotas
        for (Object controller : context.getControllers()) {
            RouterRegister.registerRoutes(server, controller, securityConfig);
        }

        // Inicia o servidor
        server.start();
        // Log visual das rotas registradas (opcional)
    }


}

