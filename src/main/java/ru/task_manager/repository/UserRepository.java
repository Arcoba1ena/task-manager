package ru.task_manager.repository;

import java.util.List;
import java.util.Optional;

import ru.task_manager.entity.Role;
import ru.task_manager.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleIn(List<Role> roles);
}
