package controllers;

import routes.GetRouter;
import requests.Request;
import requests.Response;

import java.io.IOException;

public class ControllerTeste {

    @GetRouter("/v2")
    public void helloHandler(Request req, Response res) throws IOException {
        res.send("Lets go !");
    }
}

