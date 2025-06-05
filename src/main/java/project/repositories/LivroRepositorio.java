package project.repositories;

import configurations.genericsRepositories.GenericRepository;
import configurations.genericsRepositories.annotations.Repository;
import project.entities.Livros;

@Repository
public interface LivroRepositorio extends GenericRepository<Livros, Long> {

}
