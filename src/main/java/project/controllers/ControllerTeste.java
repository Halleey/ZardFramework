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


