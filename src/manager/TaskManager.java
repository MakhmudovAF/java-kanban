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
}