package ru.task_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.task_manager.dto.UserBasicDTO;
import ru.task_manager.dto.UserDTO;
import ru.task_manager.entity.User;
import ru.task_manager.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String userManagement(Model model) {
        List<UserBasicDTO> users = userService.getAllUsersWithDTO();
        model.addAttribute("users", users);
        model.addAttribute("usersCount", users.size());
        model.addAttribute("title", "Управление пользователями");
        return "user-management";
    }

    @PostMapping
    @ResponseBody
    public User createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public User updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? "Пользователь удален" : "Пользователь не найден";
    }
}