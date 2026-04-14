package model;

/**
 * Статусы задачи в системе трекера.
 */
public enum Status {
    NEW,          // Задача создана, но не начата
    IN_PROGRESS,  // Работа ведётся
    DONE          // Задача завершена
}