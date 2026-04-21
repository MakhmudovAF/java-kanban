package model;

import java.util.Objects;

/**
 * Базовый класс задачи.
 * Содержит общие поля и поведение для Task, Epic и Subtask.
 */
public class Task {
    protected int id;
    protected String title;
    protected String description;
    protected Status status;

    public Task(String title, String description) {
        this.title = title;
        this.description = description != null ? description : "";
        this.status = Status.NEW;
    }

    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description != null ? description : "";
        this.status = Status.NEW;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Установка статуса доступна только для Task и Subtask.
     * Epic игнорирует прямой вызов — статус рассчитывается менеджером.
     */
    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        if (id == 0 || task.id == 0) return this == o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Type getType() {
        return Type.TASK;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}