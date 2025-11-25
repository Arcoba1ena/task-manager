package ru.task_manager.repository;

import java.util.List;
import ru.task_manager.entity.Attachment;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskId(Long taskId);
    List<Attachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);
    void deleteByTaskId(Long taskId);
}