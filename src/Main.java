import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();

        System.out.println("=== Создание задач ===");
        Task task1 = manager.createTask(new Task("Купить продукты", "Молоко, хлеб, яйца"));
        Task task2 = manager.createTask(new Task("Позвонить клиенту", "Обсудить ТЗ"));
        System.out.println("Созданы задачи: " + task1.getId() + ", " + task2.getId());

        System.out.println("\n=== Создание эпика с подзадачами ===");
        Epic epic = manager.createEpic(new Epic("Организовать конференцию", "Ежегодная IT-конференция"));
        Subtask sub1 = manager.createSubtask(new Subtask("Забронировать зал", "На 200 человек", epic.getId()));
        Subtask sub2 = manager.createSubtask(new Subtask("Пригласить спикеров", "5 ключевых докладчиков", epic.getId()));
        Subtask sub3 = manager.createSubtask(new Subtask("Настроить оборудование", "Проектор, звук, интернет", epic.getId()));
        System.out.println("Эпик " + epic.getId() + " содержит подзадач: " + manager.getEpicSubtasks(epic.getId()).size());

        System.out.println("\n=== Проверка статусов ===");
        System.out.println("Эпик (все подзадачи NEW): " + epic.getStatus()); // NEW

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        System.out.println("После выполнения 1/3 подзадач: " + epic.getStatus()); // IN_PROGRESS

        sub2.setStatus(Status.DONE);
        sub3.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        manager.updateSubtask(sub3);
        System.out.println("После выполнения всех подзадач: " + epic.getStatus()); // DONE

        System.out.println("\n=== Удаление подзадачи ===");
        manager.removeSubtask(sub1.getId());
        System.out.println("Подзадач в эпике после удаления: " + manager.getEpicSubtasks(epic.getId()).size()); // 2
        System.out.println("Статус эпика: " + epic.getStatus()); // DONE (оставшиеся 2 подзадачи завершены)

        System.out.println("\n=== Итоговые списки ===");
        System.out.println("Простые задачи: " + manager.getTasks().size());
        System.out.println("Эпики: " + manager.getEpics().size());
        System.out.println("Подзадачи: " + manager.getSubtasks().size());
    }
}