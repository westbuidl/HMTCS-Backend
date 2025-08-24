package uk.gov.hmcts.reform.dev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.dev.entities.TaskEntity;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    
    /**
     * Find all tasks with a specific status
     */
    List<TaskEntity> findByStatus(TaskStatus status);
    
    /**
     * Find all tasks ordered by due date
     */
    List<TaskEntity> findAllByOrderByDueDateAsc();
    
    /**
     * Find overdue tasks (due date is before current date and status is not COMPLETED or CANCELLED)
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.dueDate < :currentDate AND t.status NOT IN (:excludedStatuses)")
    List<TaskEntity> findOverdueTasks(LocalDateTime currentDate, List<TaskStatus> excludedStatuses);
    
    /**
     * Find tasks by status ordered by due date
     */
    List<TaskEntity> findByStatusOrderByDueDateAsc(TaskStatus status);
    
    /**
     * Find tasks due between two dates
     */
    List<TaskEntity> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);
    
    /**
     * Find tasks containing title or description (case-insensitive)
     */
    @Query("SELECT t FROM TaskEntity t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TaskEntity> findByTitleOrDescriptionContainingIgnoreCase(String searchTerm);
}