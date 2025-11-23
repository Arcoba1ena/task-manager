package ru.task_manager.dto;

public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String fullName;

    // Конструкторы
    public UserResponseDTO() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // Статический метод для преобразования из Entity
    public static UserResponseDTO fromEntity(ru.task_manager.entity.User user) {
        if (user == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(String.valueOf(user.getRole()));
        dto.setFullName(user.getFullName());

        return dto;
    }
}