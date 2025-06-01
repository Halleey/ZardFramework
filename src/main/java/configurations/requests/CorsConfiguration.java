package configurations.requests;

import configurations.requests.configcors.HttpHeader;
import configurations.requests.configcors.HttpMethod;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class CorsConfiguration {

    private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;

    public CorsConfiguration() {
        configure(); // chama automaticamente quando construir, evitando de ter que fazer explicitamente no main posteriormente
    }

    public void allowOrigin(String... origins) {
        this.allowedOrigins = Arrays.asList(origins);
    }

    public void allowMethod(HttpMethod... methods) {
        this.allowedMethods = Arrays.stream(methods).map(Enum::name).collect(Collectors.toList());
    }

    public void allowHeader(HttpHeader... headers) {
        this.allowedHeaders = Arrays.stream(headers).map(HttpHeader::getValue).collect(Collectors.toList());
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
