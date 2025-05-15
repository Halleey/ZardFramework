package configurations.security;

import configurations.requests.Request;
import configurations.requests.Response;

public interface FilterClass {
    void doFilter(Request request, Response response, SecurityFilter securityFilter);
}
