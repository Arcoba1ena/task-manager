package ru.task_manager.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectWithTasksDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private UserResponseDTO createdBy;
    private List<TaskBasicDTO> tasks; // Используем упрощенный DTO для задач

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserResponseDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserResponseDTO createdBy) {
        this.createdBy = createdBy;
    }

    public List<TaskBasicDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskBasicDTO> tasks) {
        this.tasks = tasks;
    }

    public static ProjectWithTasksDTO fromEntity(ru.task_manager.entity.Project project) {
        if (project == null) return null;

        ProjectWithTasksDTO dto = new ProjectWithTasksDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setCreatedBy(UserResponseDTO.fromEntity(project.getCreatedBy()));

        // Преобразуем задачи в упрощенный DTO
        if (project.getTasks() != null) {
            List<TaskBasicDTO> taskDTOs = project.getTasks().stream()
                    .map(TaskBasicDTO::fromEntity)
                    .collect(Collectors.toList());
            dto.setTasks(taskDTOs);
        }

        return dto;
    }
}