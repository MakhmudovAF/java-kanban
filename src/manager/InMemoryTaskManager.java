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
public class InMemoryTaskManager implements TaskManager {
    private int nextId;

    // Хранилища задач по типам
    private final Map<Integer, Task> tasks;
    private final Map<Integer, Epic> epics;
    private final Map<Integer, Subtask> subtasks;

    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.nextId = 1;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return nextId++;
    }

    // --- Task ---
    @Override
    public List<Task> getTasks() {
        return List.copyOf(tasks.values());
    }

    @Override
    public boolean clearTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }

        tasks.clear();
        return true;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    @Override
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

    @Override
    public boolean removeTask(int id) {
        historyManager.remove(id);
        return tasks.remove(id) != null;
    }

    // --- Epic ---
    @Override
    public List<Epic> getEpics() {
        return List.copyOf(epics.values());
    }

    @Override
    public boolean clearEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }

        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        epics.clear();
        subtasks.clear();
        return true;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic existing = epics.get(epic.getId());
        if (existing == null) return null;

        existing.setTitle(epic.getTitle());
        existing.setDescription(epic.getDescription());
        updateEpicStatus(existing);
        return existing;
    }

    @Override
    public boolean removeEpic(int id) {
        historyManager.remove(id);
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
    @Override
    public List<Subtask> getSubtasks() {
        return List.copyOf(subtasks.values());
    }

    @Override
    public boolean clearSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }

        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
        }

        return true;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return null;
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic);
        return subtask;
    }

    @Override
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

        // Пересчитываем статусы обоих эпиков
        if (oldEpic != null) updateEpicStatus(oldEpic);
        if (newEpic != null) updateEpicStatus(newEpic);

        return existing;
    }

    @Override
    public boolean removeSubtask(int id) {
        historyManager.remove(id);
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return false;

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }

        subtasks.remove(id);
        return true;
    }

    // --- Additional methods ---
    @Override
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

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private boolean updateEpicStatus(Epic epic) {
        if (epic == null) return false;

        List<Integer> epicSubtaskIds = epic.getSubtaskIds();
        if (epicSubtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return true;
        }

        int newCount = 0;
        int doneCount = 0;

        for (Integer id : epicSubtaskIds) {
            Status status = subtasks.get(id).getStatus();
            if (status == Status.NEW) newCount++;
            if (status == Status.DONE) doneCount++;
        }

        if (newCount == epicSubtaskIds.size()) {
            epic.setStatus(Status.NEW);
        } else if (doneCount == epicSubtaskIds.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }

        return true;
    }
}