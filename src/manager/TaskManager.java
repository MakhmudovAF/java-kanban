package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер задач — центральный компонент системы.
 * Управляет созданием, хранением, обновлением и удалением задач всех типов.
 */
public class TaskManager {
    private int nextId;

    // Хранилища задач по типам
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;

    public TaskManager() {
        this.nextId = 1;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    private int generateId() {
        return nextId++;
    }

    // --- Task ---
    public List<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    public boolean clearTasks() {
        tasks.clear();
        return true;
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Task createTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    public Task updateTask(Task task) {
        if (task.getId() <= 0 || !tasks.containsKey(task.getId())) {
            return null;
        }

        Task existing = tasks.get(task.getId());
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        return existing;
    }

    public boolean removeTask(int id) {
        return tasks.remove(id) != null;
    }
}