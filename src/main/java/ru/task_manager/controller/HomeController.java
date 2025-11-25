package ru.task_manager.controller;

import java.util.List;
import java.security.Principal;
import ru.task_manager.entity.*;
import java.util.stream.Collectors;
import org.springframework.ui.Model;
import ru.task_manager.service.TaskService;
import ru.task_manager.service.ProjectService;
import ru.task_manager.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class HomeController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "TaskManager - Главная");
        model.addAttribute("message", "Добро пожаловать в систему управления задачами!");
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "TaskManager - Панель управления");

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", currentUser);

        try {
            if (currentUser.getRole() == Role.EXECUTOR) {
                long userTaskCount = taskService.getTaskCountForCurrentUser(currentUser);
                model.addAttribute("totalTasks", userTaskCount);
                model.addAttribute("todoTasks", taskService.getTasksByExecutor(currentUser)
                        .stream().filter(t -> t.getStatus() == TaskStatus.TO_DO).count());
                model.addAttribute("inProgressTasks", taskService.getTasksByExecutor(currentUser)
                        .stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count());
                model.addAttribute("doneTasks", taskService.getTasksByExecutor(currentUser)
                        .stream().filter(t -> t.getStatus() == TaskStatus.DONE).count());
            } else {
                model.addAttribute("totalTasks", taskService.getTotalTaskCount());
                model.addAttribute("todoTasks", taskService.getTodoTaskCount());
                model.addAttribute("inProgressTasks", taskService.getInProgressTaskCount());
                model.addAttribute("doneTasks", taskService.getDoneTaskCount());
            }
        } catch (Exception e) {
            model.addAttribute("totalTasks", 0);
            model.addAttribute("todoTasks", 0);
            model.addAttribute("inProgressTasks", 0);
            model.addAttribute("doneTasks", 0);
        }

        return "dashboard";
    }

    @GetMapping("/tasks")
    public String tasks(@RequestParam(value = "openTask", required = false) Long openTaskId,
                        Model model, Principal principal) {
        model.addAttribute("title", "TaskManager - Управление задачами");

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<Task> userTasks = taskService.getTasksForCurrentUser(currentUser);
        model.addAttribute("tasks", userTasks);
        model.addAttribute("tasksCount", userTasks.size());
        model.addAttribute("currentUser", currentUser);

        boolean isExecutor = currentUser.getRole() == Role.EXECUTOR;
        model.addAttribute("isExecutor", isExecutor);

        if (openTaskId != null) {
            model.addAttribute("openTaskId", openTaskId);
            System.out.println("Opening task from notification: " + openTaskId);
        }

        boolean canCreateTasks = currentUser.getRole() != Role.EXECUTOR;
        model.addAttribute("canCreateTasks", canCreateTasks);

        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("executors", userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EXECUTOR)
                .collect(Collectors.toList()));

        return "tasks";
    }

    @GetMapping("/projects")
    public String projects(Model model, Principal principal) {
        model.addAttribute("title", "TaskManager - Проекты");
        var allProjects = projectService.getAllProjects();
        model.addAttribute("projects", allProjects);
        model.addAttribute("projectsCount", allProjects.size());

        boolean canCreateProjects = false;
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER)) {
                canCreateProjects = true;
            }
        }
        model.addAttribute("canCreateProjects", canCreateProjects);

        return "projects";
    }

    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        return "login";
    }

    @PostMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }
}