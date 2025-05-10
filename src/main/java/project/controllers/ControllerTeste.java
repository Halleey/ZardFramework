package project.controllers;

import configurations.instancias.RestController;
import configurations.responses.ResponseEntity;
import configurations.routes.RequestController;
import entities.JsonUtils;
import project.dtos.UserRequestDto;
import entities.Users;
import configurations.routes.DeleteRouter;
import configurations.routes.GetRouter;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.routes.PostRouter;
import project.dtos.UserResponseDTO;
import project.services.UserService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestController("/user")
public class ControllerTeste {

    private final UserService service;

    public ControllerTeste(UserService service) {
        this.service = service;
    }

    @GetRouter("")
    public String helloHandler()  {
        return "Lets go !";
    }
    @GetRouter("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello, luc");
    }

    //Versãoc Arcaica

    @GetRouter("/all")
    public String getAll() {
        // Chama o serviço para pegar todos os usuários
        List<Users> usersList = service.getAll();
        // Envia a resposta com a lista de usuários
       return JsonUtils.toJson(usersList);
    }

    //Versãoc com ResponseEntity

    @GetRouter("/todos")
    public ResponseEntity<String> pegatodos() {
        // Chama o serviço para pegar todos os usuários
        List<Users> usersList = service.getAll();

        // Converte a lista de usuários para JSON
        String jsonResponse = JsonUtils.toJson(usersList);
        // Envia a resposta com a lista de usuários
        return ResponseEntity.ok(jsonResponse);
    }


    @DeleteRouter("/delete")
    public void deleteUser(Request req, Response res) throws IOException {
        String idStr = req.extractPathParam("/user/delete");
        Long id = Long.valueOf(idStr);

        boolean deleted = service.deleteUser(id);

        if (deleted) {
            res.send("Usuário deletado com sucesso!");
        } else {
            res.send("Usuário não encontrado!");
        }
    }


    @GetRouter("/find")
    public ResponseEntity<String> findId(Request request) {
        String idStr = request.extractPathParam("/user/find");
        Long  address_id = Long.valueOf(idStr);
        System.out.println("DEBUG: ID convertido para Long: " + address_id);

        List<Users> users = service.getUserById(address_id);


        String json = JsonUtils.toJson(users);
        System.out.println("DEBUG: JSON gerado: " + json);

        System.out.println("DEBUG: Retornando ResponseEntity com JSON");
        return ResponseEntity.ok(json);
    }

    @GetRouter("/equals")
    public ResponseEntity<String> getEqualsName(Request request) {
        // Extrai o parâmetro "name" da query string
        String name = request.getQueryParam("name");

        if (name == null || name.isBlank()) {
            return ResponseEntity.status(400, "Parâmetro 'name' é obrigatório");
        }

        List<UserResponseDTO> users = service.getUsersByName(name);

        // Converte a lista em JSON
        String json = JsonUtils.toJson(users);

        // Retorna a resposta com status 200
        return ResponseEntity.ok(json);
    }

    //SAVE USER NEW MODEL
    @PostRouter("/save")
    public ResponseEntity<String> saveUser(Request req) throws IOException {
        String body = req.getBody(); // JSON vindo no body

        UserRequestDto user = JsonUtils.fromJson(body, UserRequestDto.class); // transforma JSON -> Users

        service.createUser(user); // chama o service certinho

        return ResponseEntity.status(201, "Salvando no novo modelo");
    }

    //SAVE USER OLD MODEL
    @PostRouter("/salvar")
    public void salvarVelho(Request req, Response response) throws IOException {
        String body = req.getBody(); // JSON vindo no body

        UserRequestDto user = JsonUtils.fromJson(body, UserRequestDto.class); // transforma JSON -> Users

        service.createUser(user); // chama o service certinho
        response.send("Salvando da forma velha");
    }


}


