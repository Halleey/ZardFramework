package configurations.security;

import configurations.requests.Request;
import configurations.requests.Response;
import configurations.security.auth.SecurityRouteControl;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.Set;

import static configurations.security.SecurityFilter.normalize;

public class RoleFilter implements FilterClass{

    private final SecurityRouteControl routeControl;

    public RoleFilter(SecurityRouteControl routeControl) {
        this.routeControl = routeControl;
    }

    @Override

    public void doFilter(Request request, Response response, SecurityFilter securityFilter) throws IOException, FilterException {
        String method = request.getMethod();
        String path = SecurityFilter.normalize(request.getPath()); // <- normaliza aqui

        // Verifica se a rota exige role
        if (!routeControl.requiresRole(method, path)) return;

        Claims claims = (Claims) request.getAttribute("jwt.claims");
        if (claims == null) {
            deny(response, "Token ausente ou inválido");
            return;
        }

        String userRole = claims.get("role", String.class);
        Set<String> requiredRoles = routeControl.getRolesForRoute(method, path);

        if (userRole == null || !requiredRoles.contains(userRole)) {
            deny(response, "Permissão negada");
        }
    }

    private void deny(Response response, String msg) throws IOException, FilterException {
        response.setHeader("Content-Type", "text/plain; charset=UTF-8");
        response.send(403, "403 Forbidden - " + msg);
        response.close();
        throw new FilterException("403 Forbidden - " + msg);
    }
}