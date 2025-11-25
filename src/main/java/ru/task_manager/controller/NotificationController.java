package ru.task_manager.controller;

import java.util.Map;
import java.util.List;
import java.security.Principal;
import ru.task_manager.entity.User;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import ru.task_manager.dto.NotificationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.task_manager.repository.UserRepository;
import ru.task_manager.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String notificationsPage(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<NotificationDTO> notifications = notificationService.getUserNotifications(currentUser);
        long unreadCount = notificationService.getUnreadCount(currentUser);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("title", "Уведомления");

        return "notifications";
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<?> getUserNotifications(Principal principal) {
        try {
            System.out.println("=== NOTIFICATIONS API CALLED ===");

            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            System.out.println("User: " + currentUser.getUsername());

            List<NotificationDTO> notifications = notificationService.getUserNotifications(currentUser);

            System.out.println("Found notifications: " + notifications.size());
            notifications.forEach(n -> System.out.println(" - " + n.getTitle() + " (read: " + n.isRead() + ")"));

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("ERROR in getUserNotifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка загрузки уведомлений: " + e.getMessage()));
        }
    }

    @GetMapping("/api/notifications/count")
    @ResponseBody
    public long getUnreadCount(Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return notificationService.getUnreadCount(currentUser);
    }

    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean success = notificationService.markAsRead(id, currentUser);
            if (success) {
                long unreadCount = notificationService.getUnreadCount(currentUser);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "unreadCount", unreadCount
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Уведомление не найдено"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка сервера: " + e.getMessage()));
        }
    }

    @PostMapping("/api/notifications/read-all")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        try {
            System.out.println("=== MARK ALL AS READ CALLED ===");

            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            System.out.println("Marking all notifications as read for user: " + currentUser.getUsername());

            long unreadCountBefore = notificationService.getUnreadCount(currentUser);
            System.out.println("Unread count before: " + unreadCountBefore);

            notificationService.markAllAsRead(currentUser);

            long unreadCountAfter = notificationService.getUnreadCount(currentUser);
            System.out.println("Unread count after: " + unreadCountAfter);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Все уведомления помечены как прочитанные",
                    "unreadCount", unreadCountAfter
            ));

        } catch (Exception e) {
            System.err.println("ERROR in markAllAsRead: " + e.getMessage());
            e.printStackTrace();

            String errorMessage = "Ошибка при пометке всех уведомлений как прочитанных: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (Cause: " + e.getCause().getMessage() + ")";
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", errorMessage,
                            "debug", "Проверьте настройки транзакций и @Modifying запросы"
                    ));
        }
    }

    @GetMapping("/api/notifications/unread")
    @ResponseBody
    public ResponseEntity<?> getUnreadNotifications(Principal principal) {
        try {
            System.out.println("=== UNREAD NOTIFICATIONS API CALLED ===");

            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<NotificationDTO> notifications = notificationService.getUnreadUserNotifications(currentUser);

            System.out.println("Found unread notifications: " + notifications.size());

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("ERROR in getUnreadNotifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка загрузки непрочитанных уведомлений: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/notifications/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean deleted = notificationService.deleteNotification(id, currentUser);
            if (deleted) {
                long unreadCount = notificationService.getUnreadCount(currentUser);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Уведомление удалено",
                        "unreadCount", unreadCount
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Уведомление не найдено или у вас нет прав для удаления"));
            }
        } catch (Exception e) {
            System.err.println("ERROR in deleteNotification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при удалении уведомления: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/notifications/read")
    @ResponseBody
    public ResponseEntity<?> deleteAllReadNotifications(Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean deleted = notificationService.deleteAllReadNotifications(currentUser);
            if (deleted) {
                long unreadCount = notificationService.getUnreadCount(currentUser);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Все прочитанные уведомления удалены",
                        "unreadCount", unreadCount
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "status", "info",
                        "message", "Нет прочитанных уведомлений для удаления",
                        "unreadCount", notificationService.getUnreadCount(currentUser)
                ));
            }
        } catch (Exception e) {
            System.err.println("ERROR in deleteAllReadNotifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка при удалении прочитанных уведомлений: " + e.getMessage()));
        }
    }
}