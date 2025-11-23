package ru.task_manager.repository;

import org.springframework.stereotype.Repository;
import ru.task_manager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedBy_Id(Long userId);
}