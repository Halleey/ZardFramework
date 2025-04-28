package controllers;

import entities.JsonUtils;
import entities.Product;
import requests.Request;
import requests.Response;
import routes.PostRouter;
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

        Product product = JsonUtils.fromJson(body, Product.class);
        produtoService.saveProduct(product.getNome(), product.getPrice());
        response.send("Produto salvo");

    }
}
