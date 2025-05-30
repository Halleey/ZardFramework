package project.services;

import configurations.instancias.Service;
import project.dtos.ProductRequestDto;
import project.entities.Product;
import project.repositories.ProductRepository;

import java.util.List;

@Service
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

    public List<Product> getProduct(String nome) {

        return repository.findByName(nome);
    }

    public void update(Long id, String nome) {
        Product product =  repository.findById(id).orElseThrow(() -> new RuntimeException("Produto não existe"));
        product.setNome(nome);
        repository.update(product);
    }
}
