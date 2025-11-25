package ru.task_manager.dto;

import java.time.LocalDateTime;

public class ProjectResponseDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private UserResponseDTO createdBy;

    public ProjectResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserResponseDTO getCreatedBy() { return createdBy; }
    public void setCreatedBy(UserResponseDTO createdBy) { this.createdBy = createdBy; }

    public static ProjectResponseDTO fromEntity(ru.task_manager.entity.Project project) {
        if (project == null) return null;

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setCreatedBy(UserResponseDTO.fromEntity(project.getCreatedBy()));

        return dto;
    }
}
