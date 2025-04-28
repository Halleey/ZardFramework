package services;

import dtos.ProductRequestDto;
import entities.Product;
import repositories.ProductRepository;


public class ProdutoService {

   private  final ProductRepository repository;

    public ProdutoService(ProductRepository repository) {
        this.repository = repository;

    }

    public void saveProduct(ProductRequestDto productRequestDto) {

        Product product = new Product();
        product.setNome(productRequestDto.getNome());
        product.setPrice(productRequestDto.getPrice());
        repository.save(product);
    }
}
