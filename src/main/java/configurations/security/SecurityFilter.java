package configurations.security;

import configurations.requests.Request;
import configurations.requests.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecurityFilter {
    private final List<FilterClass> filters = new ArrayList<>();

    public void addFilter(FilterClass filter) {
        filters.add(filter);
    }

    public void doFilter(Request request, Response response) throws IOException, FilterException {
        for (FilterClass filter : filters) {
            filter.doFilter(request, response, this);
            // Se algum filtro lançar FilterException, a execução para aqui
        }
    }
}
