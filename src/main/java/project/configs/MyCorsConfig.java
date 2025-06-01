package project.configs;

import configurations.requests.CorsConfiguration;
import configurations.security.EnableCors;

@EnableCors

public class MyCorsConfig extends CorsConfiguration {

    @Override
    public void configure() {
        allowOrigin("*");
        allowMethod("GET", "POST", "DELETE", "PATCH");
        allowHeader("Content-Type", "Authorization");
    }

}
