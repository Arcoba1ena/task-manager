package ru.task_manager.repository;

import java.util.List;
import ru.task_manager.entity.Project;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreatedBy_Id(Long userId);
}