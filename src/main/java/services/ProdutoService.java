package services;

import entities.Product;
import repositories.ProductRepository;


public class ProdutoService {

   private  final ProductRepository repository;

    public ProdutoService(ProductRepository repository) {
        this.repository = repository;

    }

    public void saveProduct(String nome, double price) {

        Product product = new Product();
        product.setNome(nome);
        product.setPrice(price);
        repository.save(product);
    }
}
