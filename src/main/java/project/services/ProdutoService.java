package project.services;

import configurations.instancias.Service;

import project.dtos.ProductRequestDto;

import project.entities.Product;
import project.repositories.ProductRepository;

import java.util.List;
import java.util.Optional;


@Service
public class ProdutoService {

    private  final ProductRepository repository;

    public ProdutoService(ProductRepository repository) {
        this.repository = repository;

    }
    public void saveProductWithImage(ProductRequestDto dto) {
        Product product = new Product();
        product.setNome(dto.getNome());
        product.setPrice(dto.getPrice());

        if (dto.getImage() != null) {
            product.setLargeimage(dto.getImage());
        }

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

    public Optional<Product> findById(Long id) {
     
        return  repository.findById(id);
    }
}