package configurations.requests;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final HttpExchange exchange;
    private Map<String, String> pathParams = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();

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

    public String getBody() throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public String getQueryParam(String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
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

    // ====== NOVO: atributos (úteis para filtros) ======
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
}
