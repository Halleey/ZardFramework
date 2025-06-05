package configurations.security;

import configurations.core.requests.Request;
import configurations.core.requests.Response;

import java.io.IOException;

public interface FilterClass {
    void doFilter(Request request, Response response, SecurityFilter securityFilter) throws IOException, FilterException;
}
