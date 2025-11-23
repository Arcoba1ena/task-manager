package ru.task_manager.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.task_manager.dto.NotificationDTO;
import ru.task_manager.entity.*;
import ru.task_manager.repository.NotificationRepository;
import ru.task_manager.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Создание уведомления о новой задаче
    public void notifyTaskCreated(Task task, User author) {
        // Уведомление исполнителю, если он назначен
        if (task.getExecutor() != null && !task.getExecutor().equals(author)) {
            String title = "Новая задача";
            String message = String.format("Пользователь %s назначил вам новую задачу: \"%s\"",
                    author.getFullName(), task.getTitle());
            createNotification(title, message, NotificationType.ASSIGNED_TO_TASK, task.getExecutor(), task);
        }

        // Уведомление всем менеджерам и администраторам (кроме автора)
        List<User> managersAndAdmins = getManagersAndAdmins();
        for (User user : managersAndAdmins) {
            if (!user.equals(author) && isUserRelatedToTask(user, task)) {
                String title = "Создана новая задача";
                String message = String.format("Пользователь %s создал новую задачу: \"%s\"",
                        author.getFullName(), task.getTitle());
                createNotification(title, message, NotificationType.TASK_CREATED, user, task);
            }
        }
    }

    // Создание уведомления об изменении статуса
    public void notifyStatusChange(Task task, User currentUser, TaskStatus oldStatus, TaskStatus newStatus) {
        String title = "Изменен статус задачи";
        String message = String.format("Пользователь %s изменил статус задачи \"%s\": %s → %s",
                currentUser.getFullName(), task.getTitle(), getStatusText(oldStatus), getStatusText(newStatus));

        Set<User> recipients = new HashSet<>();

        // Добавляем автора (если это не текущий пользователь)
        if (!task.getAuthor().equals(currentUser)) {
            recipients.add(task.getAuthor());
        }

        // Добавляем исполнителя (если он есть и это не текущий пользователь)
        if (task.getExecutor() != null && !task.getExecutor().equals(currentUser)) {
            recipients.add(task.getExecutor());
        }

        // Для менеджеров и администраторов: отправляем уведомление только если они не являются автором/исполнителем
        // и только если они имеют отношение к задаче (например, создатели проекта или ответственные)
        List<User> managersAndAdmins = getManagersAndAdmins();
        for (User user : managersAndAdmins) {
            // Не отправляем уведомление текущему пользователю и тем, кто уже получит уведомление как автор/исполнитель
            if (!user.equals(currentUser) && !recipients.contains(user)) {
                // Дополнительная проверка: отправляем только если пользователь связан с проектом задачи
                if (isUserRelatedToTask(user, task)) {
                    recipients.add(user);
                }
            }
        }

        // Создаем уведомления для каждого получателя
        for (User recipient : recipients) {
            createNotification(title, message, NotificationType.STATUS_CHANGED, recipient, task);
        }
    }

    // Метод для проверки связи пользователя с задачей
    private boolean isUserRelatedToTask(User user, Task task) {
        // Если задача в проекте и пользователь - создатель проекта
        if (task.getProject() != null && task.getProject().getCreatedBy().equals(user)) {
            return true;
        }

        // Если пользователь - менеджер или администратор, и задача в одном из его проектов
        if ((user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN) &&
                task.getProject() != null) {
            return true;
        }

        return false;
    }

    // Создание уведомления об обновлении задачи
    public void notifyTaskUpdated(Task task, User currentUser) {
        String title = "Задача обновлена";
        String message = String.format("Пользователь %s обновил задачу \"%s\"",
                currentUser.getFullName(), task.getTitle());

        // Уведомление автору (если это не текущий пользователь)
        if (!task.getAuthor().equals(currentUser)) {
            createNotification(title, message, NotificationType.TASK_UPDATED, task.getAuthor(), task);
        }

        // Уведомление исполнителю (если он есть и это не текущий пользователь)
        if (task.getExecutor() != null && !task.getExecutor().equals(currentUser)) {
            createNotification(title, message, NotificationType.TASK_UPDATED, task.getExecutor(), task);
        }
    }

    // Вспомогательный метод для создания уведомления
    private void createNotification(String title, String message, NotificationType type, User user, Task task) {
        Notification notification = new Notification(title, message, type, user, task);
        notificationRepository.save(notification);
    }

    // Получение списка менеджеров и администраторов
    private List<User> getManagersAndAdmins() {
        return userRepository.findByRoleIn(List.of(Role.MANAGER, Role.ADMIN));
    }

    // Преобразование статуса в текст
    private String getStatusText(TaskStatus status) {
        switch (status) {
            case TO_DO: return "К выполнению";
            case IN_PROGRESS: return "В работе";
            case DONE: return "Выполнено";
            default: return status.name();
        }
    }

    // Получение уведомлений для пользователя
    public List<NotificationDTO> getUserNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получение непрочитанных уведомлений для пользователя
    public List<NotificationDTO> getUnreadUserNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получение количества непрочитанных уведомлений
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // Пометить уведомление как прочитанное
    @Transactional
    public boolean markAsRead(Long notificationId, User user) {
        try {
            System.out.println("Marking notification as read. ID: " + notificationId + ", User: " + user.getUsername());

            int updated = notificationRepository.markAsRead(notificationId, user);

            if (updated > 0) {
                System.out.println("✓ Successfully marked notification " + notificationId + " as read");
                return true;
            } else {
                System.out.println("✗ No notification found with ID: " + notificationId + " for user: " + user.getUsername());
                return false;
            }
        } catch (Exception e) {
            System.err.println("✗ Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось обновить уведомление", e);
        }
    }

    // Пометить все уведомления как прочитанные
    @Transactional
    public void markAllAsRead(User user) {
        try {
            notificationRepository.markAllAsReadByUser(user);
            System.out.println("Все уведомления помечены как прочитанные для пользователя " + user.getUsername());

        } catch (Exception e) {
            System.err.println("Ошибка при пометке всех уведомлений как прочитанных: " + e.getMessage());
            throw new RuntimeException("Не удалось обновить уведомления", e);
        }
    }

    // Преобразование Entity в DTO
    private NotificationDTO convertToDTO(Notification notification) {
        try {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(notification.getId());
            dto.setTitle(notification.getTitle() != null ? notification.getTitle() : "Без заголовка");
            dto.setMessage(notification.getMessage() != null ? notification.getMessage() : "");
            dto.setType(notification.getType() != null ? notification.getType().name() : "UNKNOWN");
            dto.setRead(notification.isRead());

            // Используем LocalDateTime из сущности Notification
            dto.setCreatedAt(notification.getCreatedAt());

            if (notification.getTask() != null) {
                dto.setTaskId(notification.getTask().getId());
                dto.setTaskTitle(notification.getTask().getTitle() != null ?
                        notification.getTask().getTitle() : "Без названия");
            } else {
                dto.setTaskId(null);
                dto.setTaskTitle(null);
            }

            return dto;
        } catch (Exception e) {
            System.err.println("Error converting notification to DTO: " + e.getMessage());
            // Возвращаем минимальный DTO в случае ошибки
            NotificationDTO fallback = new NotificationDTO();
            fallback.setId(notification != null ? notification.getId() : -1L);
            fallback.setTitle("Ошибка загрузки уведомления");
            fallback.setMessage("Не удалось загрузить уведомление");
            fallback.setType("ERROR");
            fallback.setRead(true);
            fallback.setCreatedAt(java.time.LocalDateTime.now());
            return fallback;
        }
    }

    @Transactional
    public boolean deleteNotification(Long notificationId, User user) {
        try {
            int deleted = notificationRepository.deleteByIdAndUser(notificationId, user);
            if (deleted > 0) {
                System.out.println("Уведомление " + notificationId + " удалено пользователем " + user.getUsername());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Ошибка при удалении уведомления: " + e.getMessage());
            throw new RuntimeException("Не удалось удалить уведомление", e);
        }
    }

    @Transactional
    public boolean deleteAllReadNotifications(User user) {
        try {
            int deletedCount = notificationRepository.deleteAllReadByUser(user);
            System.out.println("Удалено " + deletedCount + " прочитанных уведомлений для пользователя " + user.getUsername());
            return deletedCount > 0;
        } catch (Exception e) {
            System.err.println("Ошибка при удалении всех прочитанных уведомлений: " + e.getMessage());
            throw new RuntimeException("Не удалось удалить прочитанные уведомления", e);
        }
    }
}