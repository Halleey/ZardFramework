package project.configs;

import configurations.security.configcors.CorsConfiguration;
import configurations.security.configcors.HttpHeader;
import configurations.security.configcors.HttpMethod;
import configurations.security.EnableCors;

@EnableCors
public class MyCorsConfig extends CorsConfiguration {

    @Override
    public void configure() {
        allowOrigin("*");
        allowMethod(HttpMethod.POST, HttpMethod.GET, HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS);
        allowHeader(HttpHeader.AUTHORIZATION, HttpHeader.CONTENT_TYPE);
    }
}
