package configurations.security;

import configurations.core.requests.Request;
import configurations.core.requests.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecurityFilter {
    private final List<FilterClass> filters = new ArrayList<>();

    public void addFilter(FilterClass... filters) {
        this.filters.addAll(Arrays.asList(filters));
    }

    public void doFilter(Request request, Response response) throws IOException, FilterException {
        for (FilterClass filter : filters) {
            filter.doFilter(request, response, this);
            // Se algum filtro lançar FilterException, a execução para aqui
        }
    }
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}

