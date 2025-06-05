package configurations.core.handlers;

import configurations.core.requests.Request;
import configurations.core.requests.Response;

import java.io.IOException;

@FunctionalInterface
public interface RequestHandler {
    void handle(Request req, Response res) throws IOException;
}

