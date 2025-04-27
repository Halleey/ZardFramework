import configurations.RouterRegister;
import configurations.Server;
import controllers.ControllerTeste;

import java.io.IOException;

public class ZardFrameworkApplication {
	public static void main(String[] args) throws IOException {
		// Criação do servidor
		Server app = new Server(8080);

		// Criar instância do controlador
		ControllerTeste controllerTeste = new ControllerTeste();
		// Registrar as rotas automaticamente com base nas anotações
		RouterRegister.registerRoutes(app, controllerTeste);
		// Iniciar o servidor
		app.start();
	}
}
