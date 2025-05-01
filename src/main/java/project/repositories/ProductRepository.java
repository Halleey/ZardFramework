package project.repositories;

import configurations.dbas.Querys;
import configurations.genericsRepositories.GenericRepository;
import entities.Product;


import java.util.List;

public interface ProductRepository extends GenericRepository<Product, Long> {

    @Querys("SELECT * FROM product WHERE nome = ?")
    List<Product> findByName(String name);


}
