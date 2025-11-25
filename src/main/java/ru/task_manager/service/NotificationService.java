package ru.task_manager.service;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import ru.task_manager.entity.*;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import ru.task_manager.dto.NotificationDTO;
import org.springframework.stereotype.Service;
import ru.task_manager.repository.UserRepository;
import ru.task_manager.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void notifyTaskCreated(Task task, User author) {
        if (task.getExecutor() != null && !task.getExecutor().equals(author)) {
            String title = "Новая задача";
            String message = String.format("Пользователь %s назначил вам новую задачу: \"%s\"",
                    author.getFullName(), task.getTitle());
            createNotification(title, message, NotificationType.ASSIGNED_TO_TASK, task.getExecutor(), task);
        }

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

    public void notifyStatusChange(Task task, User currentUser, TaskStatus oldStatus, TaskStatus newStatus) {
        String title = "Изменен статус задачи";
        String message = String.format("Пользователь %s изменил статус задачи \"%s\": %s → %s",
                currentUser.getFullName(), task.getTitle(), getStatusText(oldStatus), getStatusText(newStatus));

        Set<User> recipients = new HashSet<>();

        if (!task.getAuthor().equals(currentUser)) {
            recipients.add(task.getAuthor());
        }

        if (task.getExecutor() != null && !task.getExecutor().equals(currentUser)) {
            recipients.add(task.getExecutor());
        }

        List<User> managersAndAdmins = getManagersAndAdmins();
        for (User user : managersAndAdmins) {
            if (!user.equals(currentUser) && !recipients.contains(user)) {
                if (isUserRelatedToTask(user, task)) {
                    recipients.add(user);
                }
            }
        }

        for (User recipient : recipients) {
            createNotification(title, message, NotificationType.STATUS_CHANGED, recipient, task);
        }
    }

    private boolean isUserRelatedToTask(User user, Task task) {
        if (task.getProject() != null && task.getProject().getCreatedBy().equals(user)) {
            return true;
        }

        if ((user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN) &&
                task.getProject() != null) {
            return true;
        }

        return false;
    }

    public void notifyTaskUpdated(Task task, User currentUser) {
        String title = "Задача обновлена";
        String message = String.format("Пользователь %s обновил задачу \"%s\"",
                currentUser.getFullName(), task.getTitle());

        if (!task.getAuthor().equals(currentUser)) {
            createNotification(title, message, NotificationType.TASK_UPDATED, task.getAuthor(), task);
        }

        if (task.getExecutor() != null && !task.getExecutor().equals(currentUser)) {
            createNotification(title, message, NotificationType.TASK_UPDATED, task.getExecutor(), task);
        }
    }

    private void createNotification(String title, String message, NotificationType type, User user, Task task) {
        Notification notification = new Notification(title, message, type, user, task);
        notificationRepository.save(notification);
    }

    private List<User> getManagersAndAdmins() {
        return userRepository.findByRoleIn(List.of(Role.MANAGER, Role.ADMIN));
    }

    private String getStatusText(TaskStatus status) {
        return switch (status) {
            case TO_DO -> "К выполнению";
            case IN_PROGRESS -> "В работе";
            case DONE -> "Выполнено";
            default -> status.name();
        };
    }

    public List<NotificationDTO> getUserNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadUserNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

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

    private NotificationDTO convertToDTO(Notification notification) {
        try {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(notification.getId());
            dto.setTitle(notification.getTitle() != null ? notification.getTitle() : "Без заголовка");
            dto.setMessage(notification.getMessage() != null ? notification.getMessage() : "");
            dto.setType(notification.getType() != null ? notification.getType().name() : "UNKNOWN");
            dto.setRead(notification.isRead());
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