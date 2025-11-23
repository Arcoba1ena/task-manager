package ru.task_manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // Разрешенные MIME types
    private final String[] allowedTypes = {
            "text/plain",                                      // .txt
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",       // .xlsx
            "image/jpeg",                                      // .jpeg, .jpg
            "image/png"                                        // .png
    };

    public String storeFile(MultipartFile file) throws IOException {
        // Проверка типа файла
        if (!isAllowedFileType(file.getContentType())) {
            throw new IllegalArgumentException("Недопустимый тип файла. Разрешены: txt, docx, xlsx, jpeg, png");
        }

        // Проверка размера (максимум 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Файл слишком большой. Максимальный размер: 10MB");
        }

        // Создание директории, если не существует
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Генерация уникального имени файла
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Сохранение файла
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileName;
    }

    public byte[] loadFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("Файл не найден: " + filename);
        }
        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        Files.deleteIfExists(filePath);
    }

    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) return false;

        for (String type : allowedTypes) {
            if (type.equals(contentType)) return true;
        }
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }
}