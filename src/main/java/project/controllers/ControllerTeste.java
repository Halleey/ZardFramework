package project.controllers;

import configurations.instancias.RestController;
import configurations.responses.ResponseEntity;
import configurations.routes.*;
import entities.JsonUtils;
import project.dtos.UserRequestDto;
import entities.Users;
import configurations.requests.Request;
import configurations.requests.Response;
import project.dtos.UserResponseDTO;
import project.services.UserService;
import java.io.IOException;
import java.time.Year;
import java.util.List;
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
    public ResponseEntity<List<Users>> pegatodos() {
        // Chama o serviço para pegar todos os usuários
        List<Users> usersList = service.getAll();
        // Envia a resposta com a lista de usuários
        return ResponseEntity.ok(usersList);
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
    public ResponseEntity<List<Users>> findIdParam(@QueryParam("id") Long id) {

        List<Users> users = service.getUserById(id);

        return ResponseEntity.ok(users);
    }

    //Nova versão
    //usamos o curinga no response para podermos trabalhar tanto com serialização json
    // quanto retorno em String personalizado
    @GetRouter("/equals")
    public ResponseEntity<?> getEqualsName(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(400, "erro, parametro obrigatorio faltando");
        }

        List<UserResponseDTO> users = service.getUsersByName(name);
        return ResponseEntity.ok(users); // Objeto real!
    }



//    @GetRouter("/equals")
//    Metodo antigo e mal feito.
//    public ResponseEntity<String> getEqualsName(Request request) {
//        // Extrai o parâmetro "name" da query string
//        String name = request.getQueryParam("name");
//
//        if (name == null || name.isBlank()) {
//            return ResponseEntity.status(400, "Parâmetro 'name' é obrigatório");
//        }
//
//        List<UserResponseDTO> users = service.getUsersByName(name);
//
//        // Converte a lista em JSON
//        String json = JsonUtils.toJson(users);
//
//        // Retorna a resposta com status 200
//        return ResponseEntity.ok(json);
//    }

    //SAVE USER NEW MODEL
    //ele recebe a DTO direto via reflexão
    @PostRouter("/save")
    public ResponseEntity<String> saveUser(UserRequestDto requestDto) {
        service.createUser(requestDto); // já recebeu o DTO pronto

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


