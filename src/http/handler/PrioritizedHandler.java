package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        if ("GET".equalsIgnoreCase(h.getRequestMethod())) {
            sendText(h, gson.toJson(manager.getPrioritizedTasks()));
        } else {
            sendText(h, "{\"error\":\"Method not allowed\"}", 405);
        }
    }
}