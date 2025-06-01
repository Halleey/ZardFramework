package configurations.requests.configcors;
public enum HttpHeader {
    CONTENT_TYPE("Content-Type"),
    AUTHORIZATION("Authorization"),
    ACCEPT("Accept"),
    ORIGIN("Origin"),
    USER_AGENT("User-Agent"),
    X_REQUESTED_WITH("X-Requested-With");

    public String getValue() {
        return value;
    }

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }


}
