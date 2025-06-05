package configurations.core.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

@FunctionalInterface
public interface RequestInterceptor {
    boolean intercept(HttpExchange exchange) throws IOException;
}