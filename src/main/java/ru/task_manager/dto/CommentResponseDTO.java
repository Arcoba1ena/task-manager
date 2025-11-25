package ru.task_manager.dto;

import java.time.LocalDateTime;
import ru.task_manager.entity.Role;
import ru.task_manager.entity.User;
import ru.task_manager.entity.Comment;

public class CommentResponseDTO {
    private Long id;
    private String text;
    private Long taskId;
    private boolean canEdit;
    private boolean canDelete;
    private UserResponseDTO author;
    private LocalDateTime createdAt;

    public CommentResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserResponseDTO getAuthor() { return author; }
    public void setAuthor(UserResponseDTO author) { this.author = author; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public boolean isCanEdit() { return canEdit; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; }

    public boolean isCanDelete() { return canDelete; }
    public void setCanDelete(boolean canDelete) { this.canDelete = canDelete; }

    public static CommentResponseDTO fromEntity(Comment comment, User currentUser) {
        if (comment == null) return null;

        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setAuthor(UserResponseDTO.fromEntity(comment.getAuthor()));
        dto.setTaskId(comment.getTask() != null ? comment.getTask().getId() : null);

        if (currentUser != null) {
            boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
            boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN ||
                    currentUser.getRole() == Role.MANAGER;

            dto.setCanEdit(isAuthor);
            dto.setCanDelete(isAuthor || isAdminOrManager);
        } else {
            dto.setCanEdit(false);
            dto.setCanDelete(false);
        }

        return dto;
    }
}