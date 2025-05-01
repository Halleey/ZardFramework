package project.controllers;

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
import project.services.UserService;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @GetRouter("/all")
    public String getAll() {
        // Chama o serviço para pegar todos os usuários
        List<Users> usersList = service.getAll();

        // Converte a lista de usuários para JSON

        // Envia a resposta com a lista de usuários
       return JsonUtils.toJson(usersList);
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

    @PostRouter("/save")
    public ResponseEntity<String> saveUser(Request req) throws IOException {
        String body = req.getBody(); // JSON vindo no body

        UserRequestDto user = JsonUtils.fromJson(body, UserRequestDto.class); // transforma JSON -> Users

        service.createUser(user); // chama o service certinho

        return ResponseEntity.created("Usuário criado com sucesso");
    }
}


