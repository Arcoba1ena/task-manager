package ru.task_manager.service;

import java.util.List;
import java.util.Optional;
import ru.task_manager.entity.Task;
import ru.task_manager.entity.User;
import ru.task_manager.dto.CommentDTO;
import ru.task_manager.entity.Comment;
import org.springframework.stereotype.Service;
import ru.task_manager.repository.TaskRepository;
import ru.task_manager.repository.UserRepository;
import ru.task_manager.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Comment> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    public Comment createComment(CommentDTO commentDTO, User author) {
        Optional<Task> task = taskRepository.findById(commentDTO.getTaskId());
        if (task.isEmpty()) {
            throw new RuntimeException("Задача не найдена");
        }

        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        comment.setTask(task.get());
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, CommentDTO commentDTO, User currentUser) {
        Optional<Comment> existingComment = commentRepository.findById(commentId);
        if (existingComment.isPresent()) {
            Comment comment = existingComment.get();

            if (!comment.getAuthor().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Вы можете редактировать только свои комментарии");
            }

            comment.setText(commentDTO.getText());
            return commentRepository.save(comment);
        }
        return null;
    }

    public boolean deleteComment(Long commentId, User currentUser) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isPresent()) {
            boolean isAuthor = comment.get().getAuthor().getId().equals(currentUser.getId());
            boolean isManagerOrAdmin = currentUser.getRole() == ru.task_manager.entity.Role.ADMIN ||
                    currentUser.getRole() == ru.task_manager.entity.Role.MANAGER;

            if (isAuthor || isManagerOrAdmin) {
                commentRepository.deleteById(commentId);
                return true;
            } else {
                throw new RuntimeException("У вас нет прав для удаления этого комментария");
            }
        }
        return false;
    }

    public long getCommentCountForTask(Long taskId) {
        return commentRepository.countByTaskId(taskId);
    }
}