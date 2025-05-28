package project.controllers;

import configurations.instancias.RestController;
import configurations.responses.ResponseEntity;
import configurations.routes.*;
import project.dtos.ProductRequestDto;
import project.entities.JsonUtils;
import configurations.requests.Request;
import configurations.requests.Response;
import project.entities.Product;
import project.services.ProdutoService;

import java.io.IOException;
import java.util.List;
@RestController
@RequestController("/products")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostRouter("")
    public void saveProduct(Request request, Response response) throws IOException {

        String body = request.getBody();

        ProductRequestDto product = JsonUtils.fromJson(body, ProductRequestDto.class);
        produtoService.saveProduct(product);
        response.send("Produto salvo");

    }

    @GetRouter("")
    public void getProduct(Request request, Response response) throws IOException {
        String nome = request.getQueryParam("nome");

        List<Product> products = produtoService.getProduct(nome);

        String json = JsonUtils.toJson(products);

        response.send(json);
    }

    @PatchRouter("")
    public ResponseEntity<String> updateProductName(@QueryParam("id") Long id, @QueryParam("nome") String nome) throws IOException {
        try {
            produtoService.update(id, nome);
            return ResponseEntity.status(200, "produto atualizado com sucesso");
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400, "ID inválido.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404, e.getMessage());
        }
    }

}

