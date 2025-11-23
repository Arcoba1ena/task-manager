package ru.task_manager.dto;

import java.time.LocalDateTime;

public class AttachmentResponseDTO {
    private Long id;
    private String originalFilename;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private UserResponseDTO uploadedBy;
    private String downloadUrl;
    private String formattedFileSize;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public UserResponseDTO getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UserResponseDTO uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getFormattedFileSize() { return formattedFileSize; }
    public void setFormattedFileSize(String formattedFileSize) { this.formattedFileSize = formattedFileSize; }

    // Статический метод для преобразования из Entity
    public static AttachmentResponseDTO fromEntity(ru.task_manager.entity.Attachment attachment) {
        if (attachment == null) return null;

        AttachmentResponseDTO dto = new AttachmentResponseDTO();
        dto.setId(attachment.getId());
        dto.setOriginalFilename(attachment.getOriginalFilename());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt());
        dto.setUploadedBy(UserResponseDTO.fromEntity(attachment.getUploadedBy()));
        dto.setDownloadUrl("/api/attachments/" + attachment.getId() + "/download");
        dto.setFormattedFileSize(formatFileSize(attachment.getFileSize()));

        return dto;
    }

    private static String formatFileSize(Long size) {
        if (size == null) return "0 B";

        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}