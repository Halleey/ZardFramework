package configurations.parsers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartParser {

    // Esse método vai pegar só os campos normais do form, tipo texto (sem arquivo),
    // e coloca num mapa onde a chave é o nome do campo e o valor é o que o usuário digitou.
    public static Map<String, String> parseFields(String body, String boundary) throws IOException {
        Map<String, String> fields = new HashMap<>();
        // Primeiro divide o corpo da requisição pelo boundary, que separa as partes do form
        String[] parts = body.split("--" + boundary);

        for (String part : parts) {
            // Pra garantir que é só campo de texto (não tem filename que indica arquivo)
            if (part.contains("Content-Disposition") && !part.contains("filename=")) {
                // Pega o nome do campo no form
                String name = extractName(part);
                // Pega o valor que o usuário enviou naquele campo
                String value = extractValue(part);
                // Guarda no mapa
                fields.put(name, value);
            }
        }
        return fields;
    }

    // Esse método é parecido, mas agora pega só os arquivos que vieram no form.
    // Ele devolve um mapa onde a chave é o nome do campo do arquivo e o valor é um MultipartFile (que é tipo o arquivo completo)
    public static Map<String, MultipartFile> parseFiles(String body, String boundary) throws IOException {
        Map<String, MultipartFile> files = new HashMap<>();
        String[] parts = body.split("--" + boundary);

        for (String part : parts) {
            // Se tem filename, é porque veio arquivo ali, então pega tudo do arquivo
            if (part.contains("filename=")) {
                String name = extractName(part);               // nome do campo no form
                String filename = extractFilename(part);       // nome do arquivo mesmo, ex: foto.jpg
                String contentType = extractContentType(part); // tipo do arquivo, ex: image/png
                byte[] data = extractFileData(part);           // bytes do arquivo (conteúdo mesmo)

                // Cria o objeto MultipartFile com tudo isso e guarda no mapa
                files.put(name, new MultipartFile(name, filename, contentType, data));
            }
        }
        return files;
    }

    // Pega o nome do campo no form, tipo 'campoNome' ou 'fotoProduto'
    private static String extractName(String part) {
        Pattern pattern = Pattern.compile("name=\"(.*?)\"");
        Matcher matcher = pattern.matcher(part);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Pega o nome do arquivo, tipo 'minhaFoto.png'
    private static String extractFilename(String part) {
        Pattern pattern = Pattern.compile("filename=\"(.*?)\"");
        Matcher matcher = pattern.matcher(part);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Pega o Content-Type, que fala qual é o tipo do arquivo (imagem, pdf, etc).
    // Se não achar, devolve "application/octet-stream" que é um tipo genérico.
    private static String extractContentType(String part) {
        Pattern pattern = Pattern.compile("Content-Type: (.*?)\r?\n");
        Matcher matcher = pattern.matcher(part);
        return matcher.find() ? matcher.group(1) : "application/octet-stream";
    }

    // Aqui pega os bytes do arquivo de verdade.
    // Como o arquivo fica depois de uma quebra de linha dupla, ele separa isso e pega só a parte que interessa.
    // Usa ISO_8859_1 porque é um jeito simples de pegar os bytes "puros" sem bagunçar.
    private static byte[] extractFileData(String part) {
        String[] split = part.split("\r\n\r\n", 2);
        if (split.length == 2) {
            return split[1].trim().getBytes(StandardCharsets.ISO_8859_1);
        }
        return new byte[0];
    }

    // Pega o valor do campo de texto (a parte depois das quebras de linha duplas)
    private static String extractValue(String part) {
        String[] split = part.split("\r\n\r\n", 2);
        if (split.length == 2) {
            return split[1].trim();
        }
        return "";
    }
}
