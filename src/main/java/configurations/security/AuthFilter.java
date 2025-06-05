package configurations.security;

import configurations.core.requests.Request;
import configurations.core.requests.Response;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import project.jwt.JwtUtil;

import java.io.IOException;
public class AuthFilter implements FilterClass {
    @Override
    public void doFilter(Request request, Response response, SecurityFilter securityFilter) throws IOException, FilterException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return;
        }
//        System.out.println("===== Debug Headers =====");
//        request.getHeaders().forEach((k, v) -> System.out.println(k + ": " + v));
//        System.out.println("=========================");

        String token = request.getHeaders().get("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            deny(response);
            return;
        }

        token = token.substring(7); // Remove "Bearer "

        try {
            Jws<Claims> claims = JwtUtil.validateToken(token);

            // Injeta o conteúdo bruto do JWT
            request.setAttribute("jwt.claims", claims.getBody());

        } catch (JwtException e) {
            deny(response);
        }
    }


    private void deny(Response response) throws IOException, FilterException {
        response.setHeader("Content-Type", "text/plain; charset=UTF-8");
        response.send(403, "403 Forbidden - Token inválido ou ausente");
        response.close();
        throw new FilterException("Token inválido ou ausente");
    }
}
