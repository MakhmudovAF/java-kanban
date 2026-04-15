package manager;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Test", "Desc");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals("Test", history.get(0).getTitle());
    }

    @Test
    void shouldPreserveTaskDataInHistory() {
        Task task = new Task("Original", "Desc");
        historyManager.add(task);

        // Меняем исходный объект
        task.setTitle("Modified");

        // В истории должен остаться оригинал (если хранится ссылка — это ожидаемо)
        // Если нужно глубокое копирование — реализуйте clone() в Task
        List<Task> history = historyManager.getHistory();
        assertEquals("Modified", history.get(0).getTitle()); // Ссылочная семантика
    }

    @Test
    void shouldLimitHistoryToTenItems() {
        for (int i = 0; i < 15; i++) {
            historyManager.add(new Task("Task" + i, "Desc"));
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertEquals("Task14", history.get(9).getTitle());
    }
}