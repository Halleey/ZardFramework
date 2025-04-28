package controllers;

import entities.JsonUtils;
import dtos.UserRequestDto;
import entities.Users;
import configurations.routes.DeleteRouter;
import configurations.routes.GetRouter;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.routes.PostRouter;
import services.UserService;

import java.io.IOException;
import java.util.List;

public class ControllerTeste {

    private final UserService service;

    public ControllerTeste(UserService service) {
        this.service = service;
    }

    @GetRouter("/v2")
    public void helloHandler(Request req, Response res) throws IOException {
        res.send("Lets go !");
    }

    @GetRouter("/all")
    public void getAll(Request request, Response response) throws  IOException{
        // Chama o serviço para pegar todos os usuários
        List<Users> usersList = service.getAll();

        // Converte a lista de usuários para JSON
        String jsonResponse = JsonUtils.toJson(usersList);

        // Envia a resposta com a lista de usuários
        response.send(jsonResponse);
    }

    @DeleteRouter("/delete")
    public void deleteUser(Request req, Response res) throws IOException {
        String idStr = req.extractPathParam("/delete");
        Long id = Long.valueOf(idStr);

        boolean deleted = service.deleteUser(id);

        if (deleted) {
            res.send("Usuário deletado com sucesso!");
        } else {
            res.send("Usuário não encontrado!");
        }
    }

    @PostRouter("/save")
    public void saveUser(Request req, Response res) throws IOException {
        String body = req.getBody(); // JSON vindo no body

        UserRequestDto user = JsonUtils.fromJson(body, UserRequestDto.class); // transforma JSON -> Users

        service.createUser(user); // chama o service certinho

        res.send("Usuário salvo!");
    }
}


