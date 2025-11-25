package ru.task_manager.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String text;
    private Long taskId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private LocalDateTime createdAt;

    public CommentDTO() {}

    public CommentDTO(String text, Long taskId) {
        this.text = text;
        this.taskId = taskId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
}