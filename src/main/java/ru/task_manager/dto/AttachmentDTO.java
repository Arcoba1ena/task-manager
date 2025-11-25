package ru.task_manager.dto;

import java.time.LocalDateTime;

public class AttachmentDTO {
    private Long id;
    private Long taskId;
    private Long fileSize;
    private String filename;
    private String fileType;
    private String originalFilename;
    private LocalDateTime uploadedAt;
    private UserResponseDTO uploadedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public UserResponseDTO getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UserResponseDTO uploadedBy) { this.uploadedBy = uploadedBy; }
}