package ru.task_manager.service;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Optional;
import ru.task_manager.entity.*;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import ru.task_manager.dto.TaskDTO;
import ru.task_manager.dto.TaskResponseDTO;
import org.springframework.stereotype.Service;
import ru.task_manager.repository.UserRepository;
import ru.task_manager.repository.TaskRepository;
import ru.task_manager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

        if (taskDTO.getProjectId() != null) {
            Optional<Project> project = projectRepository.findById(taskDTO.getProjectId());
            if (project.isPresent()) {
                task.setProject(project.get());
            } else {
                task.setProject(null);
            }
        } else {
            task.setProject(null);
        }

        if (taskDTO.getExecutorId() != null) {
            Optional<User> executor = userRepository.findById(taskDTO.getExecutorId());
            if (executor.isPresent()) {
                task.setExecutor(executor.get());
            } else {
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

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getExecutors() {
        return userRepository.findAll().stream()
                .filter(user -> String.valueOf(user.getRole()).equals("EXECUTOR"))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByExecutor(User executor) {
        return taskRepository.findByExecutor_Id(executor.getId());
    }

    public List<Task> getTasksForCurrentUser(User currentUser) {
        if (currentUser.getRole() == Role.EXECUTOR) {
            return getTasksByExecutor(currentUser);
        } else {
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

            if (currentUser.getRole() == Role.EXECUTOR) {
                if (!task.getExecutor().getId().equals(currentUser.getId())) {
                    throw new RuntimeException("У вас нет прав для редактирования этой задачи");
                }
                if (taskDTO.getStatus() != null) {
                    task.setStatus(TaskStatus.valueOf(taskDTO.getStatus()));
                }
                Task savedTask = taskRepository.save(task);

                if (taskDTO.getStatus() != null && !TaskStatus.valueOf(taskDTO.getStatus()).equals(oldStatus)) {
                    notificationService.notifyStatusChange(savedTask, currentUser, oldStatus, savedTask.getStatus());
                }

                return savedTask;
            } else {
                Task savedTask = updateTaskFromDTO(task, taskDTO);

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

    public boolean canEditTask(Long taskId, User currentUser) {
        if (currentUser.getRole() != Role.EXECUTOR) {
            return true;
        }

        Optional<Task> task = taskRepository.findById(taskId);
        return task.isPresent() &&
                task.get().getExecutor() != null &&
                task.get().getExecutor().getId().equals(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getTaskStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", (int) getTotalTaskCount());
        stats.put("todo", (int) getTodoTaskCount());
        stats.put("inProgress", (int) getInProgressTaskCount());
        stats.put("done", (int) getDoneTaskCount());
        return stats;
    }
}