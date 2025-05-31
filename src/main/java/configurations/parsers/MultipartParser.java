package configurations.parsers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipartParser {

    // Recebe o corpo como byte[] e o boundary (string)
    public static Map<String, String> parseFields(byte[] bodyBytes, String boundary) throws IOException {
        Map<String, String> fields = new HashMap<>();

        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> parts = split(bodyBytes, boundaryBytes);

        for (byte[] part : parts) {
            // separa headers do body da parte
            int headerEndIndex = indexOf(part, "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            if (headerEndIndex < 0) continue;

            byte[] headerBytes = Arrays.copyOfRange(part, 0, headerEndIndex);
            byte[] bodyPartBytes = Arrays.copyOfRange(part, headerEndIndex + 4, part.length);

            String headers = new String(headerBytes, StandardCharsets.ISO_8859_1);
            if (headers.contains("Content-Disposition") && !headers.contains("filename=")) {
                String name = extractName(headers);
                if (name != null) {
                    String value = new String(bodyPartBytes, StandardCharsets.UTF_8).trim();
                    fields.put(name, value);
                }
            }
        }

        return fields;
    }

    public static Map<String, MultipartFile> parseFiles(byte[] bodyBytes, String boundary) throws IOException {
        Map<String, MultipartFile> files = new HashMap<>();

        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> parts = split(bodyBytes, boundaryBytes);

        for (byte[] part : parts) {
            int headerEndIndex = indexOf(part, "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
            if (headerEndIndex < 0) continue;

            byte[] headerBytes = Arrays.copyOfRange(part, 0, headerEndIndex);
            byte[] bodyPartBytes = Arrays.copyOfRange(part, headerEndIndex + 4, part.length);

            String headers = new String(headerBytes, StandardCharsets.ISO_8859_1);
            if (headers.contains("filename=")) {
                String name = extractName(headers);
                String filename = extractFilename(headers);
                String contentType = extractContentType(headers);

                if (name != null && filename != null) {
                    // Remove os últimos dois bytes \r\n da parte (se houver)
                    if (bodyPartBytes.length >= 2 &&
                            bodyPartBytes[bodyPartBytes.length - 2] == '\r' &&
                            bodyPartBytes[bodyPartBytes.length - 1] == '\n') {
                        bodyPartBytes = Arrays.copyOf(bodyPartBytes, bodyPartBytes.length - 2);
                    }

                    files.put(name, new MultipartFile(name, filename, contentType, bodyPartBytes));
                }
            }
        }

        return files;
    }

    // Métodos auxiliares para extrair name, filename e contentType a partir dos headers (String)
    private static String extractName(String headers) {
        Matcher matcher = Pattern.compile("name=\"([^\"]+)\"").matcher(headers);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String extractFilename(String headers) {
        Matcher matcher = Pattern.compile("filename=\"([^\"]*)\"").matcher(headers);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String extractContentType(String headers) {
        Matcher matcher = Pattern.compile("Content-Type: (.+?)\r?\n").matcher(headers);
        return matcher.find() ? matcher.group(1).trim() : "application/octet-stream";
    }

    // Função para encontrar índice de um array dentro de outro (similar a indexOf)
    private static int indexOf(byte[] array, byte[] target) {
        outer:
        for (int i = 0; i <= array.length - target.length; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    // Função para dividir um array de bytes pelo boundary (similar ao split)
    private static List<byte[]> split(byte[] source, byte[] delimiter) {
        List<byte[]> parts = new ArrayList<>();

        int start = 0;
        while (true) {
            int pos = indexOf(source, delimiter, start);
            if (pos < 0) {
                parts.add(Arrays.copyOfRange(source, start, source.length));
                break;
            }
            parts.add(Arrays.copyOfRange(source, start, pos));
            start = pos + delimiter.length;
        }
        return parts;
    }

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
