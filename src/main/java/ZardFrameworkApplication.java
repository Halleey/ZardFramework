import configurations.orm.EntityManager;
import configurations.routes.RouterRegister;
import configurations.Server;
import configurations.scanners.ZardContext;

public class ZardFrameworkApplication {
	public static void main(String[] args) throws Exception {
		Server app = new Server(8080);
		//coloque aqui, o pacote principal onde se encontra suas entidades
		EntityManager.generateSchema("entities");
		ZardContext context = new ZardContext();
		//coloque aqui, o pacote principal do seu projeto
		context.initialize("project");

		for (Object controller : context.getControllers()) {
			RouterRegister.registerRoutes(app, controller);
		}
		// De resto, não tem ncessidade alterar.
		app.start();
	}
}