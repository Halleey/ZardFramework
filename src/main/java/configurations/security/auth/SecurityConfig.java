package configurations.security.auth;

import configurations.security.FilterClass;
import configurations.security.SecurityFilter;

public abstract class SecurityConfig {
    protected final SecurityRouteControl routeControl = new SecurityRouteControl();
    protected final SecurityFilter filterChain = new SecurityFilter();

    public abstract void configure();

    public SecurityRouteControl getRouteControl() {
        return routeControl;
    }

    public SecurityFilter getFilterChain() {
        return filterChain;
    }

    protected void permit(String method, String path) {
        routeControl.addRoutes(method, path);
    }

    protected void addFilter(FilterClass filter) {
        filterChain.addFilter(filter);
    }
}
