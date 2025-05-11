package project.controllers;

import configurations.instancias.RestController;
import configurations.responses.ResponseEntity;
import configurations.routes.PostRouter;
import configurations.routes.RequestController;
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
    public ResponseEntity saveLivro(LivroDto livroDto) throws IOException {
         livroService.saveLivro(livroDto);
        return ResponseEntity.status(201, "Livro cadastrado com suceso");
    }

}
