import configurations.orm.EntityManager;
import configurations.routes.RouterRegister;
import configurations.Server;
import configurations.scanners.ZardContext;
import project.configs.MySecurityConfig;

public class ZardFrameworkApplication {
	public static void main(String[] args) throws Exception {
		Server app = new Server(8080);

		// Geração automática do schema das entidades
		EntityManager.generateSchema("entities");

		// Inicializa o contexto do seu microframework (busca controladores, etc.)
		ZardContext context = new ZardContext();
		context.initialize("project");

		// Instancia sua configuração de segurança
		MySecurityConfig securityConfig = new MySecurityConfig();
		securityConfig.configure(); // Garante que o configure() rode antes

		// Registra as rotas de cada controller
		for (Object controller : context.getControllers()) {
			RouterRegister.registerRoutes(app, controller, securityConfig);
		}

		// Inicia o servidor
		app.start();
	}
}
