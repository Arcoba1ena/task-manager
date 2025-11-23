package ru.task_manager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.task_manager.dto.TaskDTO;
import ru.task_manager.dto.TaskResponseDTO;
import ru.task_manager.entity.*;
import ru.task_manager.repository.ProjectRepository;
import ru.task_manager.repository.TaskRepository;
import ru.task_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public long getTotalTaskCount() {
        return taskRepository.count();
    }

    public long getTodoTaskCount() {
        return taskRepository.countTodoTasksNative();
    }

    public long getInProgressTaskCount() {
        return taskRepository.countInProgressTasksNative();
    }

    public long getDoneTaskCount() {
        return taskRepository.countDoneTasksNative();
    }

    @Transactional
    public Task createTask(TaskDTO taskDTO, User author) {
        try {
            Task task = new Task();
            task.setAuthor(author);
            Task savedTask = updateTaskFromDTO(task, taskDTO);

            // Создаем уведомления
            notificationService.notifyTaskCreated(savedTask, author);

            return savedTask;
        } catch (Exception e) {
            log.error("Error creating task: {}", e.getMessage());
            throw new RuntimeException("Не удалось создать задачу", e);
        }
    }

    public Task updateTask(Long id, TaskDTO taskDTO) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            return updateTaskFromDTO(task, taskDTO);
        }
        return null;
    }

    private Task updateTaskFromDTO(Task task, TaskDTO taskDTO) {
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());

        if (taskDTO.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
        }

        if (taskDTO.getPriority() != null) {
            task.setPriority(Priority.valueOf(taskDTO.getPriority()));
        }

        task.setDeadline(taskDTO.getDeadline());

        // Устанавливаем проект если указан и существует
        if (taskDTO.getProjectId() != null) {
            Optional<Project> project = projectRepository.findById(taskDTO.getProjectId());
            if (project.isPresent()) {
                task.setProject(project.get());
            } else {
                // Если проект не найден, сбрасываем привязку
                task.setProject(null);
            }
        } else {
            task.setProject(null);
        }

        // Устанавливаем исполнителя если указан и существует
        if (taskDTO.getExecutorId() != null) {
            Optional<User> executor = userRepository.findById(taskDTO.getExecutorId());
            if (executor.isPresent()) {
                task.setExecutor(executor.get());
            } else {
                // Если исполнитель не найден, сбрасываем привязку
                task.setExecutor(null);
            }
        } else {
            task.setExecutor(null);
        }

        return taskRepository.save(task);
    }

    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<TaskResponseDTO> getAllTasksWithDTO() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<TaskResponseDTO> getTaskByIdWithDTO(Long id) {
        return taskRepository.findById(id)
                .map(TaskResponseDTO::fromEntity);
    }

    // Новые методы для получения проектов и пользователей
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getExecutors() {
        // Возвращаем всех пользователей с ролью EXECUTOR
        return userRepository.findAll().stream()
                .filter(user -> String.valueOf(user.getRole()).equals("EXECUTOR"))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByExecutor(User executor) {
        return taskRepository.findByExecutor_Id(executor.getId());
    }

    public List<Task> getTasksForCurrentUser(User currentUser) {
        if (currentUser.getRole() == Role.EXECUTOR) {
            // Исполнитель видит только свои задачи
            return getTasksByExecutor(currentUser);
        } else {
            // Администратор и менеджер видят все задачи
            return getAllTasks();
        }
    }

    public long getTaskCountForCurrentUser(User currentUser) {
        if (currentUser.getRole() == Role.EXECUTOR) {
            return taskRepository.countByExecutor_Id(currentUser.getId());
        } else {
            return getTotalTaskCount();
        }
    }

    public Task updateTaskWithPermissions(Long id, TaskDTO taskDTO, User currentUser) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task task = existingTask.get();
            TaskStatus oldStatus = task.getStatus();

            // Проверяем права доступа
            if (currentUser.getRole() == Role.EXECUTOR) {
                // Исполнитель может редактировать только свои задачи
                if (!task.getExecutor().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("У вас нет прав для редактирования этой задачи");
                }
                // Исполнитель может менять только статус
                if (taskDTO.getStatus() != null) {
                    task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
                }
                // Игнорируем все остальные поля
                Task savedTask = taskRepository.save(task);

                // Создаем уведомление об изменении статуса
                if (taskDTO.getStatus() != null && !TaskStatus.valueOf(taskDTO.getStatus()).equals(oldStatus)) {
                    notificationService.notifyStatusChange(savedTask, currentUser, oldStatus, savedTask.getStatus());
                }

                return savedTask;
            } else {
                // Менеджер и администратор могут редактировать все поля
                Task savedTask = updateTaskFromDTO(task, taskDTO);

                // Создаем уведомления
                if (taskDTO.getStatus() != null && !TaskStatus.valueOf(taskDTO.getStatus()).equals(oldStatus)) {
                    notificationService.notifyStatusChange(savedTask, currentUser, oldStatus, savedTask.getStatus());
                } else {
                    notificationService.notifyTaskUpdated(savedTask, currentUser);
                }

                return savedTask;
            }
        }
        return null;
    }

    // Вспомогательный метод для проверки прав доступа к задаче
    public boolean canEditTask(Long taskId, User currentUser) {
        if (currentUser.getRole() != Role.EXECUTOR) {
            return true; // Менеджер и администратор могут редактировать любые задачи
        }

        Optional<Task> task = taskRepository.findById(taskId);
        return task.isPresent() &&
                task.get().getExecutor() != null &&
                task.get().getExecutor().getId().equals(currentUser.getId());
    }
}
