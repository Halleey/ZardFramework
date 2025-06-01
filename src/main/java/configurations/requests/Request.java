package configurations.requests;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import configurations.parsers.MultipartFile;
import configurations.parsers.MultipartParser;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public class Request {
    private final HttpExchange exchange;
    private Map<String, String> pathParams = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();

    private byte[] rawBodyBytes; // cache do corpo da requisição
    private Map<String, String> formFields;
    private Map<String, MultipartFile> formFiles;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    public String getMethod() {
        return exchange.getRequestMethod();
    }

    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    public String getQueryParam(String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headersMap = new HashMap<>();
        Headers headers = exchange.getRequestHeaders();
        for (String key : headers.keySet()) {
            headersMap.put(key, headers.getFirst(key));
        }
        return headersMap;
    }

    public void setPathParams(Map<String, String> params) {
        this.pathParams = params;
    }

    public String getPathParam(String key) {
        return pathParams.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return type.isInstance(value) ? (T) value : null;
    }

    public String getBody() throws IOException {
        return getBodyAsText();
    }


    public String getBodyAsText() throws IOException {
        return new String(getRawBodyBytes(), StandardCharsets.UTF_8);
    }

    public byte[] getRawBodyBytes() throws IOException {
        if (rawBodyBytes == null) {
            rawBodyBytes = exchange.getRequestBody().readAllBytes();
        }
        return rawBodyBytes;
    }

    public Map<String, String> getFormFields() throws IOException {
        if (formFields == null) {
            parseMultipart();
        }
        return formFields;
    }

    public Map<String, MultipartFile> getFormFiles() throws IOException {
        if (formFiles == null) {
            parseMultipart();
        }
        return formFiles;
    }

    private void parseMultipart() throws IOException {
        String contentType = getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            String boundary = contentType.split("boundary=")[1];
            byte[] bodyBytes = getRawBodyBytes();  // manter bytes
            formFields = MultipartParser.parseFields(bodyBytes, boundary);
            formFiles = MultipartParser.parseFiles(bodyBytes, boundary);
        } else {
            formFields = Collections.emptyMap();
            formFiles = Collections.emptyMap();
        }
    }

}

