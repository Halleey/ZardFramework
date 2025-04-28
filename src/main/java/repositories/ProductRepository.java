package repositories;

import entities.Product;

public class ProductRepository extends GenericRepositoryImpl<Product, Long> {

    public ProductRepository() {
        super(Product.class);
    }
}
