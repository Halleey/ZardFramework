package configurations.responses;


import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class HttpEntity<T> {

    public static final HttpEntity<?> EMPTY = new HttpEntity<>();

    private final Map<String, String> headers;
    private final T body;

    protected HttpEntity() {
        this(null, null);
    }

    public HttpEntity(T body) {
        this(body, null);
    }

    public HttpEntity(Map<String, String> headers) {
        this(null, headers);
    }

    public HttpEntity(T body, Map<String, String> headers) {
        this.body = body;
        this.headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T getBody() {
        return body;
    }

    public boolean hasBody() {
        return body != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpEntity<?> other)) return false;
        return Objects.equals(headers, other.headers) && Objects.equals(body, other.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, body);
    }

    @Override
    public String toString() {
        return "<" + (body != null ? body + "," : "") + headers + ">";
    }
}
