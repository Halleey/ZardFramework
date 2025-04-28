import configurations.EntityManager;
import configurations.RouterRegister;
import configurations.Server;
import controllers.ControllerTeste;
import entities.Users;
import repositories.GenericRepository;
import repositories.GenericRepositoryImpl;
import services.UserService;

import java.io.IOException;


public class ZardFrameworkApplication {
	public static void main(String[] args) throws IOException {
		Server app = new Server(8080);

		//Escanear o pacote para gerar as entidades.
		EntityManager.generateSchema("entities");


		// 1. Cria o repositório para Users
		GenericRepository<Users, Long> userRepository = new GenericRepositoryImpl<>(Users.class);

		// 2. Cria o UserService, passando o repositório
		UserService userService = new UserService(userRepository);

		// 3. Cria o ControllerTeste, passando o UserService
		ControllerTeste controllerTeste = new ControllerTeste(userService);
		RouterRegister.registerRoutes(app, controllerTeste);

		app.start();
	}
}
