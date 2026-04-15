package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    // --- Task ---
    List<Task> getTasks();

    boolean clearTasks();

    Task getTask(int id);

    Task createTask(Task task);

    Task updateTask(Task task);

    boolean removeTask(int id);

    // --- Epic ---
    List<Epic> getEpics();

    boolean clearEpics();

    Epic getEpic(int id);

    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    boolean removeEpic(int id);

    // --- Subtask ---
    List<Subtask> getSubtasks();

    boolean clearSubtasks();

    Subtask getSubtask(int id);

    Subtask createSubtask(Subtask subtask);

    Subtask updateSubtask(Subtask subtask);

    boolean removeSubtask(int id);

    // --- Additional methods ---
    List<Subtask> getEpicSubtasks(int epicId);

    List<Task> getHistory();
}