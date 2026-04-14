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

    // --- Epic ---
    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    public boolean clearEpics() {
        epics.clear();
        subtasks.clear();
        return true;
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Epic createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }

    public Epic updateEpic(Epic epic) {
        Epic existing = epics.get(epic.getId());
        if (existing == null) return null;

        existing.setTitle(epic.getTitle());
        existing.setDescription(epic.getDescription());
        return existing;
    }

    public boolean removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return false;

        // Удаляем все подзадачи этого эпика
        for (int subtaskId : epic.getSubtaskIds()) {
            removeSubtask(subtaskId);
        }

        epics.remove(id);
        return true;
    }

    // --- Subtask ---
    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    public boolean clearSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
        }

        return true;
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        return subtask;
    }

    public Subtask updateSubtask(Subtask subtask) {
        Subtask existing = subtasks.get(subtask.getId());
        if (existing == null) return null;

        Epic oldEpic = epics.get(existing.getEpicId());
        Epic newEpic = epics.get(subtask.getEpicId());

        // Если меняем эпик — обновляем связи
        if (existing.getEpicId() != subtask.getEpicId()) {
            oldEpic.removeSubtaskId(existing.getId());
            newEpic.addSubtaskId(subtask.getId());
        }

        existing.setTitle(subtask.getTitle());
        existing.setDescription(subtask.getDescription());
        existing.setStatus(subtask.getStatus());

        return existing;
    }

    public boolean removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return false;

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
        }

        subtasks.remove(id);
        return true;
    }

    // --- Additional methods ---
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return List.of();

        List<Subtask> epicSubtasks = new ArrayList<>();

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);

            if (subtask != null) {
                epicSubtasks.add(subtask);
            }
        }

        return epicSubtasks;
    }
}