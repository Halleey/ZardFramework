//package project.controllers;
//
//import configurations.instancias.RestController;
//import configurations.parsers.MultipartFile;
//import configurations.parsers.MultipartParser;
//import configurations.requests.Request;
//import configurations.requests.Response;
//import configurations.responses.ResponseEntity;
//import configurations.routes.GetRouter;
//import configurations.routes.PathParam;
//import configurations.routes.PostRouter;
//import configurations.routes.RequestController;
//import project.dtos.ProdutoUploadDto;
//
//
//import project.entities.Product;
//import project.services.ProdutoService;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//
//@RestController
//@RequestController("/product")
//public class ControllerProduto {
//
//    private final ProdutoService service;
//
//    public ControllerProduto(ProdutoService service) {
//        this.service = service;
//    }
//
//    @PostRouter("/upload")
//    public ResponseEntity<String> uploadProduto(Request req) throws IOException {
//        String boundary = req.getContentType().split("boundary=")[1];
//        String body = req.getBody();
//
//        Map<String, String> fields = MultipartParser.parseFields(body, boundary);
//        Map<String, MultipartFile> files = MultipartParser.parseFiles(body, boundary);
//
//        ProdutoUploadDto dto = new ProdutoUploadDto();
//        dto.setNome(fields.get("nome"));
//        dto.setPrice(new BigDecimal(fields.get("price")));
//
//        MultipartFile imagem = files.get("imagem");
//        System.out.printf("[DEBUG] Nome: %s, Price: %s%n", dto.getNome(), dto.getPrice());
//        if (imagem == null) {
//            System.out.println("[DEBUG] Imagem é null!");
//        } else {
//            System.out.printf("[DEBUG] Imagem recebida: nome=%s, tamanho=%d bytes%n",
//                    imagem.getOriginalFilename(),
//                    imagem.getBytes().length);
//        }
//
//        service.salvarProdutoComImagem(dto, imagem);
//
//        return ResponseEntity.status(201, "Produto salvo com imagem");
//    }
//    @GetRouter("/imagem/{id}")
//    public void getImagem(Request req, Response res) throws IOException {
//        Long id = Long.valueOf(req.getPathParam("id"));
//
//        Optional<Product> produtoOpt = service.findById(id);
//
//        if (produtoOpt.isEmpty() || produtoOpt.get().getLargeimage() == null) {
//            res.setStatus(404);
//            res.send("Imagem não encontrada");
//            return;
//        }
//
//        byte[] imagem = produtoOpt.get().getLargeimage();
//
//        res.setHeader("Content-Type", "image/jpeg");  // Define tipo da imagem
//        res.setHeader("Access-Control-Allow-Origin", "*"); // CORS
//        res.send(imagem);  // envia o array de bytes
//    }
//}