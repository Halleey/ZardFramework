package configurations.security;

import configurations.requests.Request;
import configurations.requests.Response;

import java.io.IOException;

public class AuthFilter implements FilterClass {
    @Override
    public void doFilter(Request request, Response response, SecurityFilter securityFilter) throws IOException, FilterException {
        String token = request.getHeaders().get("Authorization");

        if (token == null || !token.equals("teste")) {
            String forbidden = "403 Forbidden - Acesso Negado";
            response.setHeader("Content-Type", "text/plain; charset=UTF-8");
            response.send(403, forbidden);  // usa send(status, body)
            response.close();
            throw new FilterException("Acesso negado pelo filtro de autenticação.");
        }
    }
}
