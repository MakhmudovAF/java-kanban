package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    if (path.matches("/subtasks/\\d+")) {
                        int id = Integer.parseInt(path.substring(10));
                        Subtask subtask = manager.getSubtask(id);
                        if (subtask == null) {
                            sendNotFound(exchange);
                        } else {
                            sendText(exchange, gson.toJson(subtask));
                        }
                    } else {
                        sendText(exchange, gson.toJson(manager.getSubtasks()));
                    }
                    break;
                case "POST":
                    String body = readRequestText(exchange);
                    Subtask subtask = gson.fromJson(body, Subtask.class);

                    if (subtask.getId() == 0) {
                        Subtask created = manager.createSubtask(subtask);
                        if (created == null) {
                            sendNotFound(exchange);
                        } else {
                            sendText(exchange, gson.toJson(created), 201);
                        }
                    } else {
                        sendText(exchange, gson.toJson(manager.updateSubtask(subtask)), 201);
                    }
                    break;
                case "DELETE":
                    if (path.matches("/subtasks/\\d+")) {
                        int id = Integer.parseInt(path.substring(10));
                        if (manager.removeSubtask(id)) {
                            sendText(exchange, "{\"message\":\"Subtask deleted\"}");
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
            }
        } catch (IllegalArgumentException e) {
            sendHasIntersections(exchange);
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }
}