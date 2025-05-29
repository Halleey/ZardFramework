package configurations.security.auth;

import java.util.*;
public class SecurityRouteControl {

    private final Map<String, Set<String>> protectedRoutes = new HashMap<>();
    private final Set<String> publicRoutes = new HashSet<>();

    public void addRoutes(String method, String path) {
        publicRoutes.add(buildKey(method, path));
    }

    public void addProtectedRoute(String method, String path, String... roles) {
        protectedRoutes.computeIfAbsent(buildKey(method, path), k -> new HashSet<>())
                .addAll(List.of(roles));
    }

    public boolean isPublic(String method, String requestPath) {
        return match(publicRoutes, method, requestPath);
    }

    public boolean requiresRole(String method, String requestPath) {
        return match(protectedRoutes.keySet(), method, requestPath);
    }

    public Set<String> getRolesForRoute(String method, String requestPath) {
        for (String key : protectedRoutes.keySet()) {
            String[] parts = key.split(":", 2);
            String m = parts[0];
            String pattern = parts[1];
            if (m.equalsIgnoreCase(method) && pathMatches(pattern, requestPath)) {
                return protectedRoutes.get(key);
            }
        }
        return Collections.emptySet();
    }

    private boolean match(Set<String> routeSet, String method, String requestPath) {
        for (String key : routeSet) {
            String[] parts = key.split(":", 2);
            String m = parts[0];
            String pattern = parts[1];
            if (m.equalsIgnoreCase(method) && pathMatches(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean pathMatches(String pattern, String actualPath) {
        // ex: /user/delete/{id} vira /user/delete/[^/]+
        String regex = pattern.replaceAll("\\{[^/]+}", "[^/]+");
        return actualPath.matches(regex);
    }

    private String buildKey(String method, String path) {
        return method.toUpperCase() + ":" + normalize(path);
    }

    private String normalize(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}


