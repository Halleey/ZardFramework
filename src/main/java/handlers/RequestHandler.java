package handlers;

import requests.Request;
import requests.Response;

import java.io.IOException;

@FunctionalInterface
public interface RequestHandler {
    void handle(Request req, Response res) throws IOException;
}

