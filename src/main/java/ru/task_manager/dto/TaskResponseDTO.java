package ru.task_manager.dto;

import java.time.LocalDateTime;

public class TaskResponseDTO {
    private Long id;
    private String title;
    private String status;
    private String priority;
    private String description;
    private LocalDateTime deadline;
    private UserResponseDTO author;
    private LocalDateTime createdAt;
    private UserResponseDTO executor;
    private ProjectResponseDTO project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ProjectResponseDTO getProject() {
        return project;
    }

    public void setProject(ProjectResponseDTO project) {
        this.project = project;
    }

    public UserResponseDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserResponseDTO author) {
        this.author = author;
    }

    public UserResponseDTO getExecutor() {
        return executor;
    }

    public void setExecutor(UserResponseDTO executor) {
        this.executor = executor;
    }

    public static TaskResponseDTO fromEntity(ru.task_manager.entity.Task task) {
        if (task == null) return null;

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus().name());
        dto.setPriority(task.getPriority().name());
        dto.setDeadline(task.getDeadline());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setProject(ProjectResponseDTO.fromEntity(task.getProject()));
        dto.setAuthor(UserResponseDTO.fromEntity(task.getAuthor()));
        dto.setExecutor(UserResponseDTO.fromEntity(task.getExecutor()));
        return dto;
    }
}