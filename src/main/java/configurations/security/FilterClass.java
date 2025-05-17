package configurations.security;

import configurations.requests.Request;
import configurations.requests.Response;

import java.io.IOException;

public interface FilterClass {
    void doFilter(Request request, Response response, SecurityFilter securityFilter) throws IOException, FilterException;
}
