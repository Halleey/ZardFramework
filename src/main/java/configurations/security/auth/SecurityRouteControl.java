package configurations.security.auth;

import java.util.HashSet;
import java.util.Set;

public class SecurityRouteControl {

    private final Set<String> publicRoutes = new HashSet<>();


    public void addRoutes(String method, String path) {
        publicRoutes.add(method.toUpperCase() + ":" + path);
    }

    public boolean isPublic(String method, String path) {
      return   publicRoutes.contains(method.toUpperCase() + ":" + path);
    }
}

