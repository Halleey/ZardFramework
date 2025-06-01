package project.controllers;

import configurations.instancias.RestController;
import configurations.parsers.MultipartFile;
import configurations.parsers.MultipartParser;
import configurations.responses.ResponseEntity;
import configurations.routes.*;
import project.dtos.ProductRequestDto;
import project.entities.JsonUtils;
import configurations.requests.Request;
import configurations.requests.Response;
import project.entities.Product;
import project.services.ProdutoService;

import java.io.IOException;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestController("/products")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }


    @PostRouter("/upload")
    public void saveProductWithImage(Request request, Response response) throws IOException {
        String contentType = request.getContentType();

        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            response.send(400, "Tipo de conteúdo inválido.");
            return;
        }

        String boundary = contentType.split("boundary=")[1];

        byte[] bodyBytes = request.getRawBodyBytes();

        Map<String, String> fields = MultipartParser.parseFields(bodyBytes, boundary);
        Map<String, MultipartFile> files = MultipartParser.parseFiles(bodyBytes, boundary);

        //mantem na mão
        //mover para service
        ProductRequestDto dto = new ProductRequestDto();
        dto.setNome(fields.get("nome"));
        dto.setPrice(new BigDecimal(fields.get("price")));

        MultipartFile imageFile = files.get("imagem");
        if (imageFile != null && !imageFile.isEmpty()) {
            dto.setImage(imageFile.getBytes());
        }
        produtoService.saveProductWithImage(dto);

        response.send(201, "Produto com imagem salvo.");
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

    @GetRouter("/{id}/image")
    public void getProductImage(Request request, Response response) throws IOException {
        String idStr = request.getPathParam("id");

        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            response.send(400, "ID inválido");
            return;
        }

        Optional<Product> optionalProduct = produtoService.findById(id);
        if (optionalProduct.isEmpty() || optionalProduct.get().getLargeimage() == null) {
            response.send(404, "Imagem não encontrada");
            return;
        }

        byte[] imageBytes = optionalProduct.get().getLargeimage();
        response.setHeader("Content-Type", "image/jpeg"); // ou image/png dependendo do seu caso
        response.send(imageBytes);
    }


}

