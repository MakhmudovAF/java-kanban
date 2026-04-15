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
        Task task = new Task("Test", "Description");
        task.setId(1);

        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(1, history.get(0).getId());
    }

    @Test
    void shouldNotAddNullTask() {
        historyManager.add(null);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldRemoveDuplicateViews() {
        Task task1 = new Task("First", "Desc");
        task1.setId(1);
        Task task2 = new Task("Second", "Desc");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);  // Повторный просмотр

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());  // Second
        assertEquals(1, history.get(1).getId());  // First (последний просмотр)
    }

    @Test
    void shouldRemoveTaskById() {
        Task task1 = new Task("First", "Desc");
        task1.setId(1);
        Task task2 = new Task("Second", "Desc");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(2, history.get(0).getId());
        assertFalse(((InMemoryHistoryManager) historyManager).contains(1));
    }

    @Test
    void shouldReturnUnmodifiableHistoryList() {
        Task task = new Task("Test", "Desc");
        task.setId(1);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();

        assertThrows(UnsupportedOperationException.class, () -> {
            history.add(new Task("Hacked", "Nope"));
        });
    }

    @Test
    void shouldPreserveOrderOfViews() {
        for (int i = 1; i <= 5; i++) {
            Task task = new Task("Task" + i, "Desc");
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(5, history.size());

        // Порядок: первый добавленный — первый в списке
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, history.get(i).getId());
        }
    }

    @Test
    void shouldMoveTaskToEndOnRevisit() {
        Task task1 = new Task("First", "Desc");
        task1.setId(1);
        Task task2 = new Task("Second", "Desc");
        task2.setId(2);
        Task task3 = new Task("Third", "Desc");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.add(task1);  // Пересмотр task1

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(2, history.get(0).getId());  // Second
        assertEquals(3, history.get(1).getId());  // Third
        assertEquals(1, history.get(2).getId());  // First (перемещён в конец)
    }

    @Test
    void shouldHandleRemoveNonExistentTask() {
        // Не должно выбрасывать исключение
        assertDoesNotThrow(() -> historyManager.remove(999));
    }

    @Test
    void shouldSupportUnlimitedHistorySize() {
        // Добавляем 1000 задач
        for (int i = 1; i <= 1000; i++) {
            Task task = new Task("Task" + i, "Desc");
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(1000, history.size());
        assertEquals(1, history.get(0).getId());      // Первая добавленная
        assertEquals(1000, history.get(999).getId()); // Последняя добавленная
    }
}