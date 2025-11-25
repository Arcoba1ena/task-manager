package ru.task_manager.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ru.task_manager.dto.UserDTO;
import ru.task_manager.entity.Role;
import ru.task_manager.entity.User;
import ru.task_manager.dto.UserBasicDTO;
import org.springframework.stereotype.Service;
import ru.task_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        user.setFullName(userDTO.getFullName());

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserDTO userDTO) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (!user.getUsername().equals(userDTO.getUsername()) &&
                    userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
                throw new RuntimeException("Пользователь с таким именем уже существует");
            }

            if (!user.getEmail().equals(userDTO.getEmail()) &&
                    userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new RuntimeException("Пользователь с таким email уже существует");
            }

            user.setUsername(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            user.setRole(userDTO.getRole());
            user.setFullName(userDTO.getFullName());

            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            return userRepository.save(user);
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public List<UserBasicDTO> getAllUsersWithDTO() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserBasicDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<UserBasicDTO> getUserByIdWithDTO(Long id) {
        return userRepository.findById(id)
                .map(UserBasicDTO::fromEntity);
    }

    public List<User> getExecutors() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EXECUTOR)
                .collect(Collectors.toList());
    }
}