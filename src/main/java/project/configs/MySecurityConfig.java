package project.configs;

import configurations.security.AuthFilter;
import configurations.security.auth.SecurityConfig;

public class MySecurityConfig extends SecurityConfig {
    @Override
    public void configure() {
        // Liberar login e rota pública
        permit("POST", "/user/check-password");
        permit("GET", "/publico/hello");

        // Adiciona o filtro JWT para demais rotas
        addFilter(new AuthFilter());
    }
}
