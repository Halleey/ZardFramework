package project.configs;

import configurations.requests.CorsConfiguration;
import configurations.requests.configcors.HttpHeader;
import configurations.requests.configcors.HttpMethod;
import configurations.security.EnableCors;

@EnableCors

public class MyCorsConfig extends CorsConfiguration {

    @Override
    public void configure() {
        allowOrigin("*");
        allowMethod(HttpMethod.POST, HttpMethod.GET, HttpMethod.PATCH, HttpMethod.DELETE);
        allowHeader( HttpHeader.CONTENT_TYPE, HttpHeader.AUTHORIZATION);
    }

}
