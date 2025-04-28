package controllers;

import entities.JsonUtils;
import entities.Users;
import routes.GetRouter;
import requests.Request;
import requests.Response;
import routes.PostRouter;
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

    @PostRouter("/save")
    public void saveUser(Request req, Response res) throws IOException {
        String body = req.getBody(); // JSON vindo no body

        Users user = JsonUtils.fromJson(body, Users.class); // transforma JSON -> Users

        service.createUser(user.getName(), user.getEmail(), user.getCpf()); // chama o service certinho

        res.send("Usuário salvo!");
    }
}


