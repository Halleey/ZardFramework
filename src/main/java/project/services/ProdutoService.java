package project.services;

import configurations.instancias.Service;
import configurations.parsers.MultipartFile;
import project.dtos.ProdutoUploadDto;
import project.entities.Product;
import project.repositories.ProductRepository;

import java.util.Optional;

@Service
public class ProdutoService {

    private final ProductRepository repository;


    public ProdutoService(ProductRepository repository) {
        this.repository = repository;

    }

    public void salvarProdutoComImagem(ProdutoUploadDto dto, MultipartFile imagem) {
        Product produto = new Product();
        produto.setNome(dto.getNome());
        produto.setPrice(dto.getPrice());
        produto.setLargeimage(imagem.getBytes());

        repository.save(produto);
    }

    public Optional<Product> findById(Long id) {
       return repository.findById(id);
    }
}
