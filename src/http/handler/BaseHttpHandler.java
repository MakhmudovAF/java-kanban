package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected final Gson gson = new Gson();
    protected static final String CONTENT_TYPE = "application/json;charset=utf-8";

    protected void sendText(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", CONTENT_TYPE);
        h.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(response);
        }
        h.close();
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendText(h, text, 200);
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Not found\"}", 404);
    }

    protected void sendHasIntersections(HttpExchange h) throws IOException {
        sendText(h, "{\"error\":\"Task intersects with existing tasks\"}", 406);
    }

    protected void sendServerError(HttpExchange h, String message) throws IOException {
        sendText(h, "{\"error\":\"" + message + "\"}", 500);
    }

    protected String readRequestText(HttpExchange h) throws IOException {
        try (InputStream is = h.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
