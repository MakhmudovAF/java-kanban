package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Эпик — задача, состоящая из подзадач.
 * Статус эпика рассчитывается автоматически на основе статусов подзадач.
 */
public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>();
    }

    public Epic(int id, String title, String description) {
        super(id, title, description);
        this.subtaskIds = new ArrayList<>();
    }

    /**
     * Возвращает копию списка ID подзадач.
     * @return неизменяемый список идентификаторов
     */
    public List<Integer> getSubtaskIds() {
        return List.copyOf(subtaskIds);
    }

    /**
     * Добавляет ID подзадачи, если её ещё нет в списке.
     * @param subtaskId идентификатор подзадачи
     * @return true если добавление успешно
     */
    public boolean addSubtaskId(int subtaskId) {
        if (!subtaskIds.contains(subtaskId)) {
            return subtaskIds.add(subtaskId);
        }
        return false;
    }

    /**
     * Удаляет ID подзадачи из списка.
     * @param subtaskId идентификатор подзадачи
     * @return true если удаление успешно
     */
    public boolean removeSubtaskId(int subtaskId) {
        return subtaskIds.remove(Integer.valueOf(subtaskId));
    }

    /**
     * Очищает список подзадач.
     */
    public void clearSubtaskIds() {
        subtaskIds.clear();
    }

    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateEpicEndTime(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            startTime = null;
            duration = null;
            endTime = null;
            return;
        }

        List<Subtask> valid = subtasks.stream()
                .filter(subtask -> subtask.getStartTime() != null && subtask.getEndTime() != null)
                .toList();

        if (valid.isEmpty()) {
            startTime = null;
            duration = null;
            endTime = null;
            return;
        }

        startTime = valid.stream().map(Subtask::getStartTime).min(LocalDateTime::compareTo).get();
        endTime = valid.stream().map(Subtask::getEndTime).max(LocalDateTime::compareTo).get();
        duration = Duration.between(startTime, endTime);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}