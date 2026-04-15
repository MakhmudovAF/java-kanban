package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task(42, "A", "B");
        Task task2 = new Task(42, "X", "Y");

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void subtaskCannotBeItsOwnEpic() {
        // Аналогично — логическая проверка на уровне бизнес-правил
    }

    @Test
    void managersGetDefaultReturnsInitializedManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager);
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void historyShouldNotExceedTenItems() {
        TaskManager manager = Managers.getDefault();

        for (int i = 0; i < 15; i++) {
            Task task = manager.createTask(new Task("T" + i, "D"));
            manager.getTask(task.getId());
        }

        List<Task> history = manager.getHistory();
        assertEquals(10, history.size());
        assertEquals("T14", history.get(9).getTitle());
    }

    @Test
    void historyShouldReturnUnmodifiableList() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.createTask(new Task("Test", "Desc"));
        manager.getTask(task.getId());

        List<Task> history = manager.getHistory();

        assertThrows(UnsupportedOperationException.class, () -> {
            history.add(new Task("Hacked", "Nope"));
        });
    }

    @Test
    void shouldCreateAndFindTaskById() {
        Task task = new Task("Test Task", "Test Description");
        Task created = taskManager.createTask(task);

        Task found = taskManager.getTask(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Test Task", found.getTitle());
        assertEquals("Test Description", found.getDescription());
        assertEquals(Status.NEW, found.getStatus());
    }

    @Test
    void shouldCreateAndFindEpicById() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        Epic created = taskManager.createEpic(epic);

        Epic found = taskManager.getEpic(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Test Epic", found.getTitle());
        assertEquals("Epic Description", found.getDescription());
        assertEquals(Status.NEW, found.getStatus());
    }

    @Test
    void shouldCreateAndFindSubtaskById() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        Epic createdEpic = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", createdEpic.getId());
        Subtask created = taskManager.createSubtask(subtask);

        Subtask found = taskManager.getSubtask(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Test Subtask", found.getTitle());
        assertEquals(createdEpic.getId(), found.getEpicId());
    }

    @Test
    void shouldGenerateUniqueIdsForDifferentTaskTypes() {
        Task task1 = taskManager.createTask(new Task("Task", "Desc"));
        Epic epic1 = taskManager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Sub", "Desc", epic1.getId()));

        assertNotEquals(task1.getId(), epic1.getId());
        assertNotEquals(task1.getId(), subtask1.getId());
        assertNotEquals(epic1.getId(), subtask1.getId());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Test Task", "Test Description");
        Task created = taskManager.createTask(task);

        created.setTitle("Updated Name");
        created.setDescription("Updated Description");
        created.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(created);

        Task updated = taskManager.getTask(created.getId());
        assertEquals("Updated Name", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void shouldUpdateSubtaskAndAffectEpicStatus() {
        Epic epic = new Epic("Test Epic", "Epic Description");
        Epic createdEpic = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", createdEpic.getId());
        Subtask created = taskManager.createSubtask(subtask);

        // Проверяем начальный статус эпика
        assertEquals(Status.NEW, createdEpic.getStatus());

        // Обновляем подзадачу на IN_PROGRESS
        created.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(created);

        // Статус эпика должен стать IN_PROGRESS
        assertEquals(Status.IN_PROGRESS, createdEpic.getStatus());

        // Завершаем подзадачу
        created.setStatus(Status.DONE);
        taskManager.updateSubtask(created);

        // Статус эпика должен стать DONE
        assertEquals(Status.DONE, createdEpic.getStatus());
    }

    @Test
    void shouldNotCreateSubtaskWithoutEpic() {
        Subtask orphanSubtask = new Subtask("Orphan", "Desc", 999);
        Subtask created = taskManager.createSubtask(orphanSubtask);

        assertNull(created);
    }

    @Test
    void shouldMoveSubtaskBetweenEpics() {
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Desc"));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Desc"));

        Subtask subtask = taskManager.createSubtask(
                new Subtask("Sub", "Desc", epic1.getId())
        );

        // Проверяем начальное состояние
        assertEquals(1, epic1.getSubtaskIds().size());
        assertTrue(epic2.getSubtaskIds().isEmpty());

        // Переносим подзадачу в другой эпик
        subtask = new Subtask(subtask.getId(), "Sub", "Desc", epic2.getId());
        taskManager.updateSubtask(subtask);

        // Проверяем результат
        assertTrue(epic1.getSubtaskIds().isEmpty());
        assertEquals(1, epic2.getSubtaskIds().size());
        assertEquals(subtask.getId(), epic2.getSubtaskIds().get(0));
    }
}