
import configurations.orm.EntityManager;
import configurations.routes.RouterRegister;
import configurations.Server;
import configurations.scanners.ZardContext;


public class ZardFrameworkApplication {
	public static void main(String[] args) throws Exception {
		Server app = new Server(8080);
		EntityManager.generateSchema("entities");

		ZardContext context = new ZardContext();
		context.initialize("project");

		for (Object controller : context.getControllers()) {
			RouterRegister.registerRoutes(app, controller);
		}

		app.start();
	}
}