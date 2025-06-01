package configurations.parsers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartParser {

    // Recebe o corpo como byte[] e o boundary (string)
    // Faz o parsing dos campos simples (não arquivos) do corpo multipart/form-data
    public static Map<String, String> parseFields(byte[] bodyBytes, String boundary) throws IOException {
        Map<String, String> fields = new HashMap<>();

        // Converte o boundary para bytes, incluindo os dois hífens iniciais (padrão multipart)
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);

        // Divide o corpo nos blocos que correspondem a cada parte do multipart
        List<byte[]> parts = split(bodyBytes, boundaryBytes);

        // Para cada parte, extrai os headers e o corpo da parte
        for (byte[] part : parts) {
            // Busca o índice onde os headers terminam, marcado por "\r\n\r\n"
            int headerEndIndex = indexOf(part, "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            if (headerEndIndex < 0) continue;  // Se não encontrar, ignora a parte

            // Extrai os bytes dos headers e do corpo dessa parte
            byte[] headerBytes = Arrays.copyOfRange(part, 0, headerEndIndex);
            byte[] bodyPartBytes = Arrays.copyOfRange(part, headerEndIndex + 4, part.length);

            // Converte headers para string para facilitar extração
            String headers = new String(headerBytes, StandardCharsets.ISO_8859_1);

            // Se os headers indicam um campo (Content-Disposition com name e sem filename)
            if (headers.contains("Content-Disposition") && !headers.contains("filename=")) {
                // Extrai o nome do campo (name)
                String name = extractName(headers);
                if (name != null) {
                    // Converte o valor do campo (corpo da parte) para string UTF-8 e remove espaços em excesso
                    String value = new String(bodyPartBytes, StandardCharsets.UTF_8).trim();
                    // Coloca o par name=value no mapa de campos
                    fields.put(name, value);
                }
            }
        }

        return fields;
    }

    // Faz o parsing dos arquivos enviados no corpo multipart/form-data
    public static Map<String, MultipartFile> parseFiles(byte[] bodyBytes, String boundary) throws IOException {
        Map<String, MultipartFile> files = new HashMap<>();

        // Mesma lógica para obter o boundary em bytes
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> parts = split(bodyBytes, boundaryBytes);

     
        // Para cada parte, processa os arquivos
        for (byte[] part : parts) {
            // Encontra o fim dos headers da parte
            int headerEndIndex = indexOf(part, "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            if (headerEndIndex < 0) continue;

            // Separa os headers do conteúdo do arquivo
            byte[] headerBytes = Arrays.copyOfRange(part, 0, headerEndIndex);
            byte[] bodyPartBytes = Arrays.copyOfRange(part, headerEndIndex + 4, part.length);

            // Converte headers para string para extração
            String headers = new String(headerBytes, StandardCharsets.ISO_8859_1);

            // Verifica se é um arquivo (presença de filename)
            if (headers.contains("filename=")) {
                // Extrai o nome do campo, o nome do arquivo e o tipo de conteúdo
                String name = extractName(headers);
                String filename = extractFilename(headers);
                String contentType = extractContentType(headers);

                if (name != null && filename != null) {
                    // Remove os últimos dois bytes \r\n do conteúdo, se existirem (limpeza do corpo)
                    if (bodyPartBytes.length >= 2 &&
                            bodyPartBytes[bodyPartBytes.length - 2] == '\r' &&
                            bodyPartBytes[bodyPartBytes.length - 1] == '\n') {
                        bodyPartBytes = Arrays.copyOf(bodyPartBytes, bodyPartBytes.length - 2);
                    }

                    // Cria o objeto MultipartFile e adiciona no mapa pelo nome do campo
                    files.put(name, new MultipartFile(name, filename, contentType, bodyPartBytes));
                }
            }
        }

        return files;
    }

    // Extrai o valor do atributo name dos headers Content-Disposition
    private static String extractName(String headers) {
        Matcher matcher = Pattern.compile("name=\"([^\"]+)\"").matcher(headers);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Extrai o valor do atributo filename dos headers Content-Disposition
    private static String extractFilename(String headers) {
        Matcher matcher = Pattern.compile("filename=\"([^\"]*)\"").matcher(headers);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Extrai o valor do Content-Type dos headers, retorna "application/octet-stream" se não existir
    private static String extractContentType(String headers) {
        Matcher matcher = Pattern.compile("Content-Type: (.+?)\r?\n").matcher(headers);
        return matcher.find() ? matcher.group(1).trim() : "application/octet-stream";
    }

    // Função que busca o índice da primeira ocorrência do array target dentro de array
    private static int indexOf(byte[] array, byte[] target) {
        outer:
        for (int i = 0; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) continue outer;
            }
            return i;
        }
        return -1; // Não achou
    }

    // Função para dividir um array de bytes em partes separadas por um delimitador (boundary)
    private static List<byte[]> split(byte[] source, byte[] delimiter) {
        List<byte[]> parts = new ArrayList<>();

        int start = 0;
        while (true) {
            // Busca a posição do próximo boundary a partir de 'start'
            int pos = indexOf(source, delimiter, start);
            if (pos < 0) {
                // Se não encontrou mais boundaries, adiciona o restante e termina
                parts.add(Arrays.copyOfRange(source, start, source.length));
                break;
            }
            // Adiciona a parte desde 'start' até a posição do boundary
            parts.add(Arrays.copyOfRange(source, start, pos));
            // Atualiza o início para logo após o boundary encontrado
            start = pos + delimiter.length;
        }
        return parts;
    }

    // Versão do indexOf que começa a busca a partir de um índice 'from'
    private static int indexOf(byte[] array, byte[] target, int from) {
        outer:
        for (int i = from; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
