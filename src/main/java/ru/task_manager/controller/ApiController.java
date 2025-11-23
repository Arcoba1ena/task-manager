package ru.task_manager.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ru.task_manager.dto.*;
import ru.task_manager.entity.*;
import ru.task_manager.repository.UserRepository;
import ru.task_manager.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private AttachmentService attachmentService;

    // ========== TASK ENDPOINTS ==========
    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "TaskManager API работает! 🎉");
        response.put("status", "success");
        response.put("version", "1.0.0");
        return response;
    }

    @GetMapping("/tasks")
    public List<TaskResponseDTO> getTasks(Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (currentUser.getRole() == Role.EXECUTOR) {
            // Исполнитель получает только свои задачи
            List<Task> userTasks = taskService.getTasksByExecutor(currentUser);
            return userTasks.stream()
                    .map(TaskResponseDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            return taskService.getAllTasksWithDTO();
        }
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskResponseDTO> getTask(@PathVariable Long id) {
        Optional<TaskResponseDTO> task = taskService.getTaskByIdWithDTO(id);
        return task.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody TaskDTO taskDTO, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (currentUser.getRole() == Role.EXECUTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Недостаточно прав для создания задач"));
            }

            // Передаем текущего пользователя как автора
            Task task = taskService.createTask(taskDTO, currentUser);
            TaskResponseDTO responseDTO = TaskResponseDTO.fromEntity(task);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при создании задачи");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Task task = taskService.updateTaskWithPermissions(id, taskDTO, currentUser);
            if (task != null) {
                TaskResponseDTO responseDTO = TaskResponseDTO.fromEntity(task);
                return ResponseEntity.ok(responseDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обновлении задачи");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/tasks/count")
    public Map<String, Object> getTasksCount() {
        Map<String, Object> counts = new HashMap<>();
        try {
            counts.put("total", taskService.getTotalTaskCount());
            counts.put("todo", taskService.getTodoTaskCount());
            counts.put("inProgress", taskService.getInProgressTaskCount());
            counts.put("done", taskService.getDoneTaskCount());
            counts.put("status", "success");
        } catch (Exception e) {
            counts.put("status", "error");
            counts.put("message", e.getMessage());
            counts.put("total", 0);
            counts.put("todo", 0);
            counts.put("inProgress", 0);
            counts.put("done", 0);
        }
        return counts;
    }

    // ========== PROJECT ENDPOINTS ==========
    @GetMapping("/projects")
    public List<ProjectResponseDTO> getProjects() {
        return projectService.getAllProjectsWithDTO();
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectResponseDTO> getProject(@PathVariable Long id) {
        Optional<ProjectResponseDTO> project = projectService.getProjectByIdWithDTO(id);
        return project.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/projects")
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO projectDTO, Principal principal) {
        try {
            // Получаем текущего пользователя
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Устанавливаем создателя проекта - текущий пользователь
            projectDTO.setCreatedById(currentUser.getId());

            Project project = projectService.createProject(projectDTO);
            ProjectResponseDTO responseDTO = ProjectResponseDTO.fromEntity(project);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при создании проекта");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        try {
            Project project = projectService.updateProject(id, projectDTO);
            if (project != null) {
                ProjectResponseDTO responseDTO = ProjectResponseDTO.fromEntity(project);
                return ResponseEntity.ok(responseDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обновлении проекта");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        boolean deleted = projectService.deleteProject(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/projects/count")
    public Map<String, Object> getProjectsCount() {
        Map<String, Object> counts = new HashMap<>();
        try {
            counts.put("total", projectService.getTotalProjectCount());
            counts.put("status", "success");
        } catch (Exception e) {
            counts.put("status", "error");
            counts.put("message", e.getMessage());
            counts.put("total", 0);
        }
        return counts;
    }

    @GetMapping("/projects/{id}/with-tasks")
    public ResponseEntity<ProjectWithTasksDTO> getProjectWithTasks(@PathVariable Long id) {
        Optional<ProjectWithTasksDTO> project = projectService.getProjectWithTasksById(id);
        return project.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // ========== USER ENDPOINTS (ADMIN ONLY) ==========
    @GetMapping("/admin/users")
    public List<UserBasicDTO> getUsers() {
        return userService.getAllUsersWithDTO();
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<UserBasicDTO> getUser(@PathVariable Long id) {
        Optional<UserBasicDTO> user = userService.getUserByIdWithDTO(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/users")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            User user = userService.createUser(userDTO);
            UserBasicDTO responseDTO = UserBasicDTO.fromEntity(user);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при создании пользователя");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            User user = userService.updateUser(id, userDTO);
            if (user != null) {
                UserBasicDTO responseDTO = UserBasicDTO.fromEntity(user);
                return ResponseEntity.ok(responseDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обновлении пользователя");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== COMMENT ENDPOINTS ==========
    @GetMapping("/tasks/{taskId}/comments")
    public List<CommentResponseDTO> getTaskComments(@PathVariable Long taskId, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            List<Comment> comments = commentService.getCommentsByTaskId(taskId);
            return comments.stream()
                    .map(comment -> CommentResponseDTO.fromEntity(comment, currentUser))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Возвращаем пустой список в случае ошибки
            return new ArrayList<>();
        }
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> createComment(@PathVariable Long taskId,
                                           @RequestBody CommentDTO commentDTO,
                                           Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            commentDTO.setTaskId(taskId);
            Comment comment = commentService.createComment(commentDTO, currentUser);
            CommentResponseDTO responseDTO = CommentResponseDTO.fromEntity(comment, currentUser);
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при создании комментария");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Long id,
                                           @RequestBody CommentDTO commentDTO,
                                           Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Comment comment = commentService.updateComment(id, commentDTO, currentUser);
            if (comment != null) {
                CommentResponseDTO responseDTO = CommentResponseDTO.fromEntity(comment, currentUser);
                return ResponseEntity.ok(responseDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при обновлении комментария");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean deleted = commentService.deleteComment(id, currentUser);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при удалении комментария");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== ATTACHMENT ENDPOINTS ==========

    @GetMapping("/tasks/{taskId}/attachments")
    public List<AttachmentResponseDTO> getTaskAttachments(@PathVariable Long taskId) {
        return attachmentService.getAttachmentsByTaskId(taskId);
    }

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> uploadAttachment(@PathVariable Long taskId,
                                              @RequestParam("file") MultipartFile file,
                                              Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            AttachmentResponseDTO attachment = attachmentService.uploadAttachment(taskId, file, currentUser);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при загрузке файла");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            byte[] fileContent = attachmentService.downloadAttachment(attachmentId);
            Optional<Attachment> attachment = attachmentService.getAttachmentById(attachmentId);

            if (attachment.isPresent()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", attachment.get().getOriginalFilename());

                return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId, Principal principal) {
        try {
            User currentUser = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean deleted = attachmentService.deleteAttachment(attachmentId, currentUser);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Ошибка при удалении файла");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}