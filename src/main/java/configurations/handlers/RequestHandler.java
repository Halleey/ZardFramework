package configurations.handlers;

import configurations.requests.Request;
import configurations.requests.Response;

import java.io.IOException;

@FunctionalInterface
public interface RequestHandler {
    void handle(Request req, Response res) throws IOException;
}

