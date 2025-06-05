package configurations.core.responses;

import java.util.Map;
import java.util.Objects;


public class ResponseEntity<T> extends HttpEntity<T> {

    private final int status;

    public ResponseEntity(int status) {
        this(null, null, status);
    }

    public ResponseEntity(T body, int status) {
        this(body, null, status);
    }

    public ResponseEntity(Map<String, String> headers, int status) {
        this(null, headers, status);
    }

    public ResponseEntity(T body, Map<String, String> headers, int status) {
        super(body, headers);
        this.status = status;
    }

    public int getStatusCode() {
        return status;
    }

    @Override
    public String toString() {
        return "ResponseEntity<status=" + status + ", body=" + getBody() + ", headers=" + getHeaders() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof ResponseEntity<?> other)) return false;
        return status == other.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), status);
    }

    // Métodos utilitários

    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, Map.of("Content-Type", "text/plain"), 200);
    }

    public static <T> ResponseEntity<T> ok(T body, Map<String, String> headers) {
        return new ResponseEntity<>(body, headers, 200);
    }

    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, Map.of("Content-Type", "text/plain"), 201);
    }

    public static ResponseEntity<Void> noContent() {
        return new ResponseEntity<>(204);
    }

    public static <T> ResponseEntity<T> badRequest(T body) {
        return new ResponseEntity<>(body, Map.of("Content-Type", "text/plain"), 400);
    }

    public static <T> ResponseEntity<T> status(int status, T body) {
        return new ResponseEntity<>(body, Map.of("Content-Type", "text/plain"), status);
    }




    public static <T> ResponseEntity<T> json(T body) {
        return new ResponseEntity<>(body, Map.of("Content-Type", "application/json"), 200);
    }

    public static <T> ResponseEntity<T> json(int status, T body) {
        return new ResponseEntity<>(body, Map.of("Content-Type", "application/json"), status);
    }
}
