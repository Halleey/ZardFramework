package repositories;

import entities.Product;
import configurations.genericsRepositories.GenericRepositoryImpl;

public class ProductRepository extends GenericRepositoryImpl<Product, Long> {

    public ProductRepository() {
        super(Product.class);
    }
}
