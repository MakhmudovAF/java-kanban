package manager;

import java.io.File;
import java.nio.file.Path;

public class Managers {
    private Managers() {
        // Утилитарный класс
    }

    /**
     * Возвращает менеджера задач в памяти по умолчанию.
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    /**
     * Возвращает файлового менеджера задач с автосохранением.
     * @param filePath путь к файлу для сохранения данных
     */
    public static TaskManager getDefault(Path filePath) {
        return new FileBackedTaskManager(filePath);
    }

    /**
     * Возвращает менеджера истории по умолчанию.
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}