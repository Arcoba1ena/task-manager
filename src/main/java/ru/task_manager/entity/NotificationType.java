package ru.task_manager.entity;

public enum NotificationType {
    TASK_CREATED,      // Создана новая задача
    TASK_UPDATED,      // Задача обновлена
    STATUS_CHANGED,    // Изменен статус задачи
    ASSIGNED_TO_TASK,  // Назначен на задачу
    TASK_COMPLETED     // Задача завершена
}