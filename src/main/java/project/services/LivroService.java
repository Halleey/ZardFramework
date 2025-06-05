package project.services;

import configurations.genericsRepositories.annotations.Service;
import project.entities.Livros;
import project.dtos.LivroDto;
import project.repositories.LivroRepositorio;
@Service
public class LivroService {


    private final LivroRepositorio livroRepositorio;

    public LivroService(LivroRepositorio livroRepositorio) {
        this.livroRepositorio = livroRepositorio;
    }


    public void saveLivro(LivroDto livroDto){
        Livros livros = new Livros();
        livros.setTitulo(livroDto.getTitulo());
        livroRepositorio.save(livros);

    }

}


