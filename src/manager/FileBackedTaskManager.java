package manager;

import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Менеджер задач с автосохранением в файл.
 * Наследуется от InMemoryTaskManager для повторного использования логики.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String HEADER = "id,type,name,status,description,epic";
    private static final String DELIMITER = ",";

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл.
     * @throws ManagerSaveException при ошибке ввода-вывода
     */
    public void save() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, StandardCharsets.UTF_8))) {

            // Записываем заголовок
            writer.write(HEADER);
            writer.newLine();

            // Записываем простые задачи
            for (Task task : tasks.values()) {
                writer.write(toString(task));
                writer.newLine();
            }

            // Записываем эпики
            for (Epic epic : epics.values()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            // Записываем подзадачи
            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + file.getPath(), e);
        }
    }

    /**
     * Загружает состояние менеджера из файла.
     * @param file файл с данными
     * @return новый экземпляр FileBackedTaskManager с восстановленными данными
     * @throws ManagerSaveException при ошибке чтения или некорректном формате
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        if (!file.exists() || file.length() == 0) {
            return manager; // Пустой менеджер для нового файла
        }

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] lines = content.split("\\r?\\n");

            // Пропускаем заголовок, начинаем с индекса 1
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                Task task = fromString(line);
                if (task == null) continue;

                // Размещаем задачу в нужное хранилище
                if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                    // Добавляем ссылку в эпик
                    Epic epic = manager.epics.get(((Subtask) task).getEpicId());
                    if (epic != null) {
                        epic.addSubtaskId(task.getId());
                    }
                } else if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                }

                // Обновляем nextId, чтобы новые задачи не конфликтовали
                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }
            }

            // Восстанавливаем статусы эпиков после загрузки подзадач
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла: " + file.getPath(), e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Некорректный формат файла: " + file.getPath(), e);
        }

        return manager;
    }

    /**
     * Преобразует задачу в строку CSV.
     */
    private String toString(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append(task.getId()).append(DELIMITER);

        // Определяем тип задачи
        if (task instanceof Subtask) {
            sb.append(Type.SUBTASK).append(DELIMITER);
        } else if (task instanceof Epic) {
            sb.append(Type.EPIC).append(DELIMITER);
        } else {
            sb.append(Type.TASK).append(DELIMITER);
        }

        sb.append(escape(task.getTitle())).append(DELIMITER);
        sb.append(task.getStatus()).append(DELIMITER);
        sb.append(escape(task.getDescription()));

        // Для подзадач добавляем ID эпика
        if (task instanceof Subtask) {
            sb.append(DELIMITER).append(((Subtask) task).getEpicId());
        }

        return sb.toString();
    }

    /**
     * Экранирует запятые и кавычки в строке для CSV.
     */
    private String escape(String value) {
        if (value == null) return "";
        // Простая защита: если есть запятая или кавычка — оборачиваем в кавычки
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Создаёт задачу из строки CSV.
     */
    private static Task fromString(String value) {
        String[] parts = value.split(DELIMITER, -1); // -1 сохраняет пустые части в конце

        if (parts.length < 5) {
            throw new IllegalArgumentException("Некорректная строка: " + value);
        }

        int id = Integer.parseInt(parts[0].trim());
        Type type = Type.valueOf(parts[1].trim().toUpperCase());
        String title = unescape(parts[2]);
        Status status = Status.valueOf(parts[3].trim().toUpperCase());
        String description = unescape(parts[4]);

        switch (type) {
            case TASK:
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                if (parts.length < 6) {
                    throw new IllegalArgumentException("Подзадача без epicId: " + value);
                }

                int epicId = Integer.parseInt(parts[5].trim());
                Subtask subtask = new Subtask(title, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    /**
     * Деэкранирует строку из CSV.
     */
    private static String unescape(String value) {
        if (value == null || value.isEmpty()) return "";
        // Если значение в кавычках — убираем их и восстанавливаем экранированные кавычки
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }

    // ===== Переопределение модифицирующих методов =====

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public Task updateTask(Task task) {
        Task updated = super.updateTask(task);
        if (updated != null) {
            save();
        }
        return updated;
    }

    @Override
    public boolean removeTask(int id) {
        boolean removed = super.removeTask(id);
        if (removed) {
            save();
        }
        return removed;
    }

    @Override
    public boolean clearTasks() {
        boolean cleared = super.clearTasks();
        if (cleared) {
            save();
        }
        return cleared;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updated = super.updateEpic(epic);
        if (updated != null) {
            save();
        }
        return updated;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean removed = super.removeEpic(id);
        if (removed) {
            save();
        }
        return removed;
    }

    @Override
    public boolean clearEpics() {
        boolean cleared = super.clearEpics();
        if (cleared) {
            save();
        }
        return cleared;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask created = super.createSubtask(subtask);
        save();
        return created;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updated = super.updateSubtask(subtask);
        if (updated != null) {
            save();
        }
        return updated;
    }

    @Override
    public boolean removeSubtask(int id) {
        boolean removed = super.removeSubtask(id);
        if (removed) {
            save();
        }
        return removed;
    }

    @Override
    public boolean clearSubtasks() {
        boolean cleared = super.clearSubtasks();
        if (cleared) {
            save();
        }
        return cleared;
    }

    public static void main(String[] args) {
        // Создаём временный файл для демонстрации
        File tempFile;
        try {
            tempFile = File.createTempFile("task-manager", ".csv");
        } catch (IOException e) {
            System.err.println("Не удалось создать временный файл");
            return;
        }

        System.out.println("=== Создание задач ===");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task1 = manager.createTask(new Task("Купить продукты", "Молоко, хлеб, яйца"));
        Task task2 = manager.createTask(new Task("Позвонить клиенту", "Обсудить ТЗ"));
        System.out.println("Созданы задачи: " + task1.getId() + ", " + task2.getId());

        System.out.println("\n=== Создание эпика с подзадачами ===");
        Epic epic = manager.createEpic(new Epic("Организовать конференцию", "Ежегодная IT-конференция"));
        Subtask sub1 = manager.createSubtask(new Subtask("Забронировать зал", "На 200 человек", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Пригласить спикеров", "5 ключевых докладчиков", epic.getId()));

        System.out.println("\n=== Проверка сохранения ===");
        System.out.println("Файл сохранён: " + tempFile.getAbsolutePath());
        System.out.println("Размер файла: " + tempFile.length() + " байт");

        // Просматриваем задачи для добавления в историю
        manager.getTask(task1.getId());
        manager.getEpic(epic.getId());

        System.out.println("\n=== Загрузка из файла ===");
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        System.out.println("Загружено задач: " + loadedManager.getTasks().size());
        System.out.println("Загружено эпиков: " + loadedManager.getEpics().size());
        System.out.println("Загружено подзадач: " + loadedManager.getSubtasks().size());

        // Проверяем целостность данных
        Epic loadedEpic = loadedManager.getEpic(epic.getId());
        if (loadedEpic != null) {
            System.out.println("Эпик '" + loadedEpic.getTitle() + "' содержит подзадач: " +
                    loadedEpic.getSubtaskIds().size());
        }

        // Проверяем историю
        System.out.println("История просмотров: " + loadedManager.getHistory().size() + " записей");

        // Очищаем временный файл
        tempFile.delete();
        System.out.println("\n=== Демонстрация завершена ===");
    }
}