package ru.task_manager.repository;

import java.util.List;
import ru.task_manager.entity.Task;
import ru.task_manager.entity.TaskStatus;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByExecutor_Id(Long userId);
    List<Task> findByExecutor_Id(Long userId);
    List<Task> findByProject_Id(Long projectId);
    List<Task> findByAuthor_Id(Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'TO_DO'")
    long countTodoTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'IN_PROGRESS'")
    long countInProgressTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'DONE'")
    long countDoneTasks();

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE status = 'TO_DO'", nativeQuery = true)
    long countTodoTasksNative();

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE status = 'IN_PROGRESS'", nativeQuery = true)
    long countInProgressTasksNative();

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE status = 'DONE'", nativeQuery = true)
    long countDoneTasksNative();
}