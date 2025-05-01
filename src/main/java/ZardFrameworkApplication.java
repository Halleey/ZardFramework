import configurations.genericsRepositories.RepositoryFactory;
import configurations.orm.EntityManager;
import configurations.routes.RouterRegister;
import configurations.Server;
import project.controllers.ControllerTeste;
import project.controllers.ProdutoController;
import entities.Address;
import entities.Product;
import entities.Users;
import project.repositories.AddressRepository;
import project.repositories.ProductRepository;
import project.repositories.UserRepository;
import project.services.AddressService;
import project.services.ProdutoService;
import project.services.UserService;

import java.io.IOException;


public class ZardFrameworkApplication {
	public static void main(String[] args) throws IOException {
		Server app = new Server(8080);

		//Escanear o pacote para gerar as entidades.
		EntityManager.generateSchema("entities");


		// 1. Cria o repositório para Users
		UserRepository  userRepository = RepositoryFactory.createRepository(UserRepository.class, Users.class);
		ProductRepository repository = RepositoryFactory.createRepository(ProductRepository.class, Product.class);
		AddressRepository addressRepository = RepositoryFactory.createRepository(AddressRepository.class, Address.class);
		// 2. Cria o UserService, passando o repositório
		UserService userService = new UserService(userRepository, addressRepository);
		ProdutoService produtoService = new ProdutoService(repository);
		AddressService addressService = new AddressService(addressRepository);

		// 3. Cria o ControllerTeste, passando o UserService
		ControllerTeste controllerTeste = new ControllerTeste(userService);
		ProdutoController produtoController = new  ProdutoController(produtoService);
		RouterRegister.registerRoutes(app, produtoController);
		RouterRegister.registerRoutes(app, controllerTeste);



		app.start();
	}
}
