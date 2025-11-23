package ru.task_manager.dto;

import ru.task_manager.entity.Role;

import java.time.LocalDateTime;

public class UserBasicDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String fullName;
    private LocalDateTime createdAt;

    // Конструкторы
    public UserBasicDTO() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Статический метод для преобразования из Entity
    public static UserBasicDTO fromEntity(ru.task_manager.entity.User user) {
        if (user == null) return null;

        UserBasicDTO dto = new UserBasicDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setFullName(user.getFullName());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }
}