package ru.task_manager.dto;

import ru.task_manager.entity.Role;

public class UserDTO {
    private Long id;
    private Role role;
    private String email;
    private String username;
    private String password;
    private String fullName;

    public UserDTO() {}

    public UserDTO(String username, String password, String email, Role role, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}