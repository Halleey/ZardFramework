package configurations.requests;

import java.util.List;
import java.util.Arrays;
public abstract class CorsConfiguration {

    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;

    public CorsConfiguration() {
        configure(); // chama automaticamente quando construir, evitando de ter que fazer explicitamente no main posteriormente
    }

    public CorsConfiguration allowOrigin(String... origins) {
        this.allowedOrigins = Arrays.asList(origins);
        return this;
    }

    public CorsConfiguration allowMethod(String... methods) {
        this.allowedMethods = Arrays.asList(methods);
        return this;
    }

    public CorsConfiguration allowHeader(String... headers) {
        this.allowedHeaders = Arrays.asList(headers);
        return this;
    }

    public String getAllowedOriginsHeader() {
        ensureConfigured(allowedOrigins, "origins");
        return String.join(", ", allowedOrigins);
    }

    public String getAllowedMethodsHeader() {
        ensureConfigured(allowedMethods, "methods");
        return String.join(", ", allowedMethods);
    }

    public String getAllowedHeadersHeader() {
        ensureConfigured(allowedHeaders, "headers");
        return String.join(", ", allowedHeaders);
    }

    private void ensureConfigured(List<String> list, String field) {
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("CorsConfiguration: o campo '" + field + "' não foi configurado.");
        }
    }

    public abstract void configure();

    /**
     * Verifica se o método HTTP está na lista de métodos permitidos (ignora case).
     */
    public boolean isMethodAllowed(String method) {
        ensureConfigured(allowedMethods, "methods");
        return allowedMethods.stream()
                .anyMatch(m -> m.equalsIgnoreCase(method));
    }
}
