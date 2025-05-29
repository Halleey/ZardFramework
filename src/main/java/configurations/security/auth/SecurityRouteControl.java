package configurations.security.auth;

import java.util.*;

public class SecurityRouteControl {

    private final Set<String> publicRoutes = new HashSet<>();
    private final Map<String, Set<String>> protectedRoutes = new HashMap<>();

    public void addRoutes(String method, String path) {
        publicRoutes.add(method.toUpperCase() + ":" + path);
    }

    public boolean isPublic(String method, String path) {
      return   publicRoutes.contains(method.toUpperCase() + ":" + path);
    }

    public void addProtectedRoute(String method, String path, String... roles) {
        String key = method.toUpperCase() + ":" + path;
        protectedRoutes.computeIfAbsent(key, k -> new HashSet<>()).addAll(List.of(roles));
    }

    public Set<String> getRolesForRoute(String method, String path) {
        return protectedRoutes.getOrDefault(method.toUpperCase() + ":" + path, Collections.emptySet());
    }

    public boolean requiresRole(String method, String path) {
        return protectedRoutes.containsKey(method.toUpperCase() + ":" + path);
    }
}

