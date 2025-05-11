package project.controllers;

import configurations.instancias.RestController;
import configurations.requests.Request;

import configurations.responses.ResponseEntity;
import configurations.routes.PostRouter;
import configurations.routes.RequestController;
import entities.JsonUtils;
import project.dtos.LivroDto;
import project.services.LivroService;

import java.io.IOException;

@RestController
@RequestController("/livros")
public class ControllerLivros {


    private final LivroService livroService;

    public ControllerLivros(LivroService livroService) {
        this.livroService = livroService;
    }

    @PostRouter("")
    public ResponseEntity saveLivro(Request request) throws IOException {
        String body = request.getBody();

        LivroDto livros = JsonUtils.fromJson(body, LivroDto.class);
        livroService.saveLivro(livros);
        return ResponseEntity.status(201, "Livro cadastrado com suceso");
    }

}
