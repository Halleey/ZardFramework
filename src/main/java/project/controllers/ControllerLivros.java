package project.controllers;

import configurations.core.routes.annotations.RestController;
import configurations.core.responses.ResponseEntity;
import configurations.core.routes.annotations.PostRouter;
import configurations.core.routes.annotations.RequestController;
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
    public ResponseEntity<String> saveLivro(LivroDto livroDto) throws IOException {
         livroService.saveLivro(livroDto);
        return ResponseEntity.status(201, "Livro cadastrado com suceso");
    }
}
