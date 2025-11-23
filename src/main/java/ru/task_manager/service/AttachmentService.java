package ru.task_manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.task_manager.dto.AttachmentResponseDTO;
import ru.task_manager.entity.Attachment;
import ru.task_manager.entity.Task;
import ru.task_manager.entity.User;
import ru.task_manager.repository.AttachmentRepository;
import ru.task_manager.repository.TaskRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public AttachmentResponseDTO uploadAttachment(Long taskId, MultipartFile file, User uploadedBy) throws IOException {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            throw new RuntimeException("Задача не найдена");
        }

        // Сохраняем файл на диск
        String storedFilename = fileStorageService.storeFile(file);

        // Создаем запись в БД
        Attachment attachment = new Attachment();
        attachment.setFilename(storedFilename);
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFilePath("uploads/" + storedFilename);
        attachment.setTask(task.get());
        attachment.setUploadedBy(uploadedBy);

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return AttachmentResponseDTO.fromEntity(savedAttachment);
    }

    public List<AttachmentResponseDTO> getAttachmentsByTaskId(Long taskId) {
        List<Attachment> attachments = attachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
        return attachments.stream()
                .map(AttachmentResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<Attachment> getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId);
    }

    public boolean deleteAttachment(Long attachmentId, User currentUser) throws IOException {
        Optional<Attachment> attachment = attachmentRepository.findById(attachmentId);
        if (attachment.isPresent()) {
            Attachment att = attachment.get();

            // Проверяем права: автор вложения или администратор/менеджер
            boolean isAuthor = att.getUploadedBy().getId().equals(currentUser.getId());
            boolean isAdminOrManager = currentUser.getRole() == ru.task_manager.entity.Role.ADMIN ||
                    currentUser.getRole() == ru.task_manager.entity.Role.MANAGER;

            if (isAuthor || isAdminOrManager) {
                // Удаляем файл с диска
                fileStorageService.deleteFile(att.getFilename());
                // Удаляем запись из БД
                attachmentRepository.deleteById(attachmentId);
                return true;
            } else {
                throw new RuntimeException("У вас нет прав для удаления этого файла");
            }
        }
        return false;
    }

    public byte[] downloadAttachment(Long attachmentId) throws IOException {
        Optional<Attachment> attachment = attachmentRepository.findById(attachmentId);
        if (attachment.isPresent()) {
            return fileStorageService.loadFile(attachment.get().getFilename());
        }
        throw new RuntimeException("Файл не найден");
    }
}