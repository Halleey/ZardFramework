package project.configs;

import configurations.security.AuthFilter;
import configurations.security.EnableSecurity;
import configurations.security.RoleFilter;
import configurations.security.auth.SecurityConfig;

@EnableSecurity
public class MySecurityConfig extends SecurityConfig {
    @Override
    public void configure() {
        // Liberar login e rota pública.
        permit("POST", "/user/check-password");
        permit("GET", "/publico/hello");
        permit("POST", "/user/login");
        permit("POST", "/user/save");
        hasRole("DELETE", "/user/delete/{id}", "admin");
        hasRole("GET", "/user", "admin");
        //adiciona o tipo de filtro a ser usado.
        addFilter(new AuthFilter(),  new RoleFilter(getRouteControl()));
    }
}
