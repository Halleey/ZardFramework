import configurations.genericsRepositories.RepositoryFactory;
import configurations.orm.EntityManager;
import configurations.routes.RouterRegister;
import configurations.Server;
import controllers.ControllerTeste;
import controllers.ProdutoController;
import entities.Product;
import repositories.ProductRepository;
import repositories.UserRepository;
import services.ProdutoService;
import services.UserService;

import java.io.IOException;


public class ZardFrameworkApplication {
	public static void main(String[] args) throws IOException {
		Server app = new Server(8080);

		//Escanear o pacote para gerar as entidades.
		EntityManager.generateSchema("entities");


		// 1. Cria o repositório para Users
		UserRepository  userRepository = new UserRepository();
		ProductRepository repository = RepositoryFactory.createRepository(ProductRepository.class, Product.class);



		// 2. Cria o UserService, passando o repositório
		UserService userService = new UserService(userRepository);
		ProdutoService produtoService = new ProdutoService(repository);


		// 3. Cria o ControllerTeste, passando o UserService
		ControllerTeste controllerTeste = new ControllerTeste(userService);
		ProdutoController produtoController = new  ProdutoController(produtoService);
		RouterRegister.registerRoutes(app, produtoController);
		RouterRegister.registerRoutes(app, controllerTeste);

		app.start();
	}
}
