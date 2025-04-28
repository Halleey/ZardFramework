package controllers;

import dtos.ProductRequestDto;
import entities.JsonUtils;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.routes.PostRouter;
import services.ProdutoService;

import java.io.IOException;

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
}
