package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager, Gson gson) {
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
                    if (path.matches("/tasks/\\d+")) {
                        int id = Integer.parseInt(path.substring(7));
                        Task task = manager.getTask(id);
                        if (task == null) {
                            sendNotFound(exchange);
                        } else {
                            sendText(exchange, gson.toJson(task));
                        }
                    } else {
                        sendText(exchange, gson.toJson(manager.getTasks()));
                    }
                    break;
                case "POST":
                    String body = readRequestText(exchange);
                    Task task = gson.fromJson(body, Task.class);

                    if (task.getId() == 0) {
                        sendText(exchange, gson.toJson(manager.createTask(task)), 201);
                    } else {
                        sendText(exchange, gson.toJson(manager.updateTask(task)), 201);
                    }
                    break;
                case "DELETE":
                    if (path.matches("/tasks/\\d+")) {
                        int id = Integer.parseInt(path.substring(7));
                        if (manager.removeTask(id)) {
                            sendText(exchange, "{\"message\":\"Task deleted\"}");
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