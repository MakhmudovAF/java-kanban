package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Менеджер задач — центральный компонент системы.
 * Управляет созданием, хранением, обновлением и удалением задач всех типов.
 */
public class InMemoryTaskManager implements TaskManager {
    protected int nextId;

    // Хранилища задач по типам
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;

    protected final HistoryManager historyManager;

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime).thenComparing(Task::getId)
    );

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

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null && task.getEndTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    private void syncPrioritized(Task task, boolean shouldAdd) {
        removeFromPrioritized(task);
        if (shouldAdd) addToPrioritized(task);
    }

    private boolean hasIntersection(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getEndTime() == null ||
                t2.getStartTime() == null || t2.getEndTime() == null) {
            return false;
        }

        return t1.getEndTime().isAfter(t2.getStartTime());
    }

    private void validateIntersection(Task task) {
        boolean hasConflict = prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> hasIntersection(t, task));

        if (hasConflict) {
            throw new IllegalArgumentException("Задача пересекается по времени выполнения с другой задачей");
        }
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
        validateIntersection(task);
        tasks.put(id, task);
        addToPrioritized(task);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        Task existing = tasks.get(task.getId());
        if (existing == null) return null;
        validateIntersection(task);
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        existing.setDuration(task.getDuration());
        existing.setStartTime(task.getStartTime());
        syncPrioritized(existing, true);
        return existing;
    }

    @Override
    public boolean removeTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritized(task);
            historyManager.remove(id);
        }
        return task != null;
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
        existing.updateEpicEndTime(getEpicSubtasks(existing.getId()));
        return existing;
    }

    @Override
    public boolean removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            List<Integer> subIds = new ArrayList<>(epic.getSubtaskIds());
            for (Integer subId : subIds) removeSubtask(subId);
            historyManager.remove(id);
        }
        return epic != null;
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
            updateEpic(epic);
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
        validateIntersection(subtask);
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        addToPrioritized(subtask);
        updateEpic(epic);
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask existing = subtasks.get(subtask.getId());
        if (existing == null) return null;
        validateIntersection(subtask);

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
        existing.setStartTime(subtask.getStartTime());
        existing.setDuration(subtask.getDuration());

        syncPrioritized(existing, true);

        // Пересчитываем статусы обоих эпиков
        if (oldEpic != null) updateEpic(oldEpic);
        if (newEpic != null) updateEpic(newEpic);

        return existing;
    }

    @Override
    public boolean removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) return false;

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpic(epic);
        }
        removeFromPrioritized(subtask);
        historyManager.remove(id);
        return true;
    }

    // --- Additional methods ---
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return List.of();

        return epic.getSubtaskIds().stream().map(subtasks::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void updateEpicStatus(Epic epic) {
        if (epic == null) return;

        List<Integer> epicSubtaskIds = epic.getSubtaskIds();
        if (epicSubtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int newCount = 0;
        int doneCount = 0;

        for (Integer id : epicSubtaskIds) {
            Subtask subtask = subtasks.get(id);
            if (subtask == null) continue;
            Status status = subtask.getStatus();
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
    }
}