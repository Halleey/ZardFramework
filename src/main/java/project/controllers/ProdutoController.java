package project.controllers;

import configurations.routes.GetRouter;
import configurations.routes.PatchRouter;
import configurations.routes.RequestController;
import project.dtos.ProductRequestDto;
import entities.JsonUtils;
import configurations.requests.Request;
import configurations.requests.Response;
import configurations.routes.PostRouter;
import entities.Product;
import project.services.ProdutoService;

import java.io.IOException;
import java.util.List;

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
    public void updateProductName(Request request, Response response) throws IOException {
        // Captura o id e o novo nome da requisição
        String idParam = request.getQueryParam("id");
        String novoNome = request.getQueryParam("nome");

        if (idParam == null || novoNome == null) {
            response.send(400, "parametro errado");
            response.send("Parâmetros 'id' e 'nome' são obrigatórios para atualização.");
            return;
        }

        try {
            Long id = Long.parseLong(idParam); // Converte o ID para Long
            produtoService.update(id, novoNome); // Atualiza no serviço
            response.send("Produto atualizado com sucesso.");
        } catch (NumberFormatException e) {
            response.send(400, "campo errado ou inexistente");
            response.send("ID inválido.");
        } catch (RuntimeException e) {
            response.send(404, "rota não existe");
            response.send(e.getMessage());
        }
    }
}

