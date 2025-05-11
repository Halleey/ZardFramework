package project.repositories;

import configurations.genericsRepositories.GenericRepository;
import configurations.instancias.Repository;
import entities.Livros;

@Repository
public interface LivroRepositorio extends GenericRepository<Livros, Long> {

}
