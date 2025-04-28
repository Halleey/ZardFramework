package controllers;

import configurations.routes.GetRouter;
import dtos.ProductRequestDto;
import entities.JsonUtils;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.routes.PostRouter;
import entities.Product;
import services.ProdutoService;

import java.io.IOException;
import java.util.List;

public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostRouter("/product")
    public void saveProduct(Request request, Response response) throws IOException {

        String body = request.getBody();

        ProductRequestDto product = JsonUtils.fromJson(body, ProductRequestDto.class);
        produtoService.saveProduct(product);
        response.send("Produto salvo");

    }
    @GetRouter("/product")
    public void getProduct(Request request, Response response) throws IOException {
        String nome = request.getQueryParam("nome");

        List<Product> products = produtoService.getProduct(nome);

        String json = JsonUtils.toJson(products);

        response.send(json);
    }

}
