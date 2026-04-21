package manager;

import java.io.File;

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
     * @param file файл для сохранения данных
     */
    public static TaskManager getDefault(File file) {
        return new FileBackedTaskManager(file);
    }

    /**
     * Возвращает менеджера истории по умолчанию.
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}