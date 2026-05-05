package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
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
                    if (path.matches("/epics/\\d+/subtasks")) {
                        String[] parts = path.split("/");
                        int epicId = Integer.parseInt(parts[2]);
                        if (manager.getEpic(epicId) == null) {
                            sendNotFound(exchange);
                        } else {
                            sendText(exchange, gson.toJson(manager.getEpicSubtasks(epicId)));
                        }
                    } else if (path.matches("/epics/\\d+")) {
                        int id = Integer.parseInt(path.substring(6));
                        Epic epic = manager.getEpic(id);
                        if (epic == null) {
                            sendNotFound(exchange);
                        } else {
                            sendText(exchange, gson.toJson(epic));
                        }
                    } else {
                        sendText(exchange, gson.toJson(manager.getEpics()));
                    }
                    break;
                case "POST":
                    String body = readRequestText(exchange);
                    Epic epic = gson.fromJson(body, Epic.class);

                    if (epic.getId() == 0) {
                        sendText(exchange, gson.toJson(manager.createEpic(epic)), 201);
                    } else {
                        sendText(exchange, gson.toJson(manager.updateEpic(epic)), 201);
                    }
                    break;
                case "DELETE":
                    if (path.matches("/epics/\\d+")) {
                        int id = Integer.parseInt(path.substring(6));
                        if (manager.removeEpic(id)) {
                            sendText(exchange, "{\"message\":\"Epic deleted\"}");
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }
}