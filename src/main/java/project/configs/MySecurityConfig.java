package project.configs;

import configurations.security.AuthFilter;
import configurations.security.EnableSecurity;
import configurations.security.auth.SecurityConfig;

@EnableSecurity
public class MySecurityConfig extends SecurityConfig {
    @Override
    public void configure() {
        // Liberar login e rota pública.
        permit("POST", "/user/check-password");
        permit("GET", "/publico/hello");

        //adiciona o tipo de filtro a ser usado.
        addFilter(new AuthFilter());
    }
}
