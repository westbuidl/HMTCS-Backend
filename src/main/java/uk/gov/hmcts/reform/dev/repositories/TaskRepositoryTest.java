/*package uk.gov.hmcts.reform.dev.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.dev.entities.TaskEntity;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.hibernate.SQL=DEBUG"
})
@DisplayName("TaskRepository Tests")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private TaskEntity pendingTask;
    private TaskEntity inProgressTask;
    private TaskEntity completedTask;
    private TaskEntity overdueTask;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        // Clear any existing data
        taskRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test tasks
        pendingTask = new TaskEntity("Pending Task", "Description 1", TaskStatus.PENDING, testDateTime.plusDays(2));
        inProgressTask = new TaskEntity("In Progress Task", "Description 2", TaskStatus.IN_PROGRESS, testDateTime.plusDays(1));
        completedTask = new TaskEntity("Completed Task", "Description 3", TaskStatus.COMPLETED, testDateTime.plusDays(3));
        overdueTask = new TaskEntity("Overdue Task", "Description 4", TaskStatus.PENDING, testDateTime.minusDays(1));

        // Persist test data
        entityManager.persist(pendingTask);
        entityManager.persist(inProgressTask);
        entityManager.persist(completedTask);
        entityManager.persist(overdueTask);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and retrieve task by ID")
    void shouldSaveAndRetrieveTaskById() {
        // Given
        TaskEntity newTask = new TaskEntity("New Task", "New Description", TaskStatus.PENDING, testDateTime.plusDays(1));

        // When
        TaskEntity savedTask = taskRepository.save(newTask);
        Optional<TaskEntity> retrievedTask = taskRepository.findById(savedTask.getId());

        // Then
        assertThat(retrievedTask).isPresent();
        assertThat(retrievedTask.get().getTitle()).isEqualTo("New Task");
        assertThat(retrievedTask.get().getDescription()).isEqualTo("New Description");
        assertThat(retrievedTask.get().getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(retrievedTask.get().getId()).isNotNull();
        assertThat(retrievedTask.get().getCreatedDate()).isNotNull();
        assertThat(retrievedTask.get().getUpdatedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should find tasks by status")
    void shouldFindTasksByStatus() {
        // When
        List<TaskEntity> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        List<TaskEntity> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);
        List<TaskEntity> completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETED);

        // Then
        assertThat(pendingTasks).hasSize(2); // pendingTask and overdueTask
        assertThat(inProgressTasks).hasSize(1);
        assertThat(completedTasks).hasSize(1);

        assertThat(pendingTasks).extracting(TaskEntity::getStatus)
                .containsOnly(TaskStatus.PENDING);
        assertThat(inProgressTasks).extracting(TaskEntity::getStatus)
                .containsOnly(TaskStatus.IN_PROGRESS);
        assertThat(completedTasks).extracting(TaskEntity::getStatus)
                .containsOnly(TaskStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should find all tasks ordered by due date ascending")
    void shouldFindAllTasksOrderedByDueDateAscending() {
        // When
        List<TaskEntity> tasks = taskRepository.findAllByOrderByDueDateAsc();

        // Then
        assertThat(tasks).hasSize(4);
        assertThat(tasks.get(0)).isEqualTo(overdueTask); // Earliest due date (in the past)
        assertThat(tasks.get(1)).isEqualTo(inProgressTask); // testDateTime + 1 day
        assertThat(tasks.get(2)).isEqualTo(pendingTask); // testDateTime + 2 days
        assertThat(tasks.get(3)).isEqualTo(completedTask); // testDateTime + 3 days
    }

    @Test
    @DisplayName("Should find overdue tasks")
    void shouldFindOverdueTasks() {
        // Given
        LocalDateTime currentTime = testDateTime; // Current time for test
        List<TaskStatus> excludedStatuses = Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED);

        // When
        List<TaskEntity> overdueTasks = taskRepository.findOverdueTasks(currentTime, excludedStatuses);

        // Then
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks.get(0)).isEqualTo(overdueTask);
        assertThat(overdueTasks.get(0).getDueDate()).isBefore(currentTime);
        assertThat(overdueTasks.get(0).getStatus()).isNotIn(excludedStatuses);
    }

    @Test
    @DisplayName("Should not find completed tasks as overdue")
    void shouldNotFindCompletedTasksAsOverdue() {
        // Given - Create a completed overdue task
        TaskEntity completedOverdueTask = new TaskEntity("Completed Overdue", "Description", 
                                                         TaskStatus.COMPLETED, testDateTime.minusDays(2));
        entityManager.persist(completedOverdueTask);
        entityManager.flush();

        LocalDateTime currentTime = testDateTime;
        List<TaskStatus> excludedStatuses = Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED);

        // When
        List<TaskEntity> overdueTasks = taskRepository.findOverdueTasks(currentTime, excludedStatuses);

        // Then
        assertThat(overdueTasks).hasSize(1);
        assertThat(overdueTasks).doesNotContain(completedOverdueTask);
        assertThat(overdueTasks.get(0)).isEqualTo(overdueTask);
    }

    @Test
    @DisplayName("Should find tasks by status ordered by due date")
    void shouldFindTasksByStatusOrderedByDueDate() {
        // Given - Add another pending task with different due date
        TaskEntity anotherPendingTask = new TaskEntity("Another Pending", "Description", 
                                                      TaskStatus.PENDING, testDateTime.plusDays(4));
        entityManager.persist(anotherPendingTask);
        entityManager.flush();

        // When
        List<TaskEntity> pendingTasksOrdered = taskRepository.findByStatusOrderByDueDateAsc(TaskStatus.PENDING);

        // Then
        assertThat(pendingTasksOrdered).hasSize(3);
        // Should be ordered: overdueTask (past), pendingTask (+2 days), anotherPendingTask (+4 days)
        assertThat(pendingTasksOrdered.get(0)).isEqualTo(overdueTask);
        assertThat(pendingTasksOrdered.get(1)).isEqualTo(pendingTask);
        assertThat(pendingTasksOrdered.get(2)).isEqualTo(anotherPendingTask);
    }

    @Test
    @DisplayName("Should find tasks due between dates")
    void shouldFindTasksDueBetweenDates() {
        // Given
        LocalDateTime startDate = testDateTime.plusDays(1);
        LocalDateTime endDate = testDateTime.plusDays(2);

        // When
        List<TaskEntity> tasksDueBetween = taskRepository.findByDueDateBetween(startDate, endDate);

        // Then
        assertThat(tasksDueBetween).hasSize(2);
        assertThat(tasksDueBetween).containsExactlyInAnyOrder(inProgressTask, pendingTask);
    }

    @Test
    @DisplayName("Should count tasks by status")
    void shouldCountTasksByStatus() {
        // When
        long pendingCount = taskRepository.countByStatus(TaskStatus.PENDING);
        long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long cancelledCount = taskRepository.countByStatus(TaskStatus.CANCELLED);

        // Then
        assertThat(pendingCount).isEqualTo(2);
        assertThat(inProgressCount).isEqualTo(1);
        assertThat(completedCount).isEqualTo(1);
        assertThat(cancelledCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should find tasks by title or description containing search term (case insensitive)")
    void shouldFindTasksByTitleOrDescriptionContaining() {
        // When - Search by title
        List<TaskEntity> tasksByTitle = taskRepository.findByTitleOrDescriptionContainingIgnoreCase("pending");
        
        // When - Search by description
        List<TaskEntity> tasksByDescription = taskRepository.findByTitleOrDescriptionContainingIgnoreCase("description 2");
        
        // When - Search with different case
        List<TaskEntity> tasksByCaseInsensitive = taskRepository.findByTitleOrDescriptionContainingIgnoreCase("COMPLETED");

        // Then
        assertThat(tasksByTitle).hasSize(1);
        assertThat(tasksByTitle.get(0)).isEqualTo(pendingTask);

        assertThat(tasksByDescription).hasSize(1);
        assertThat(tasksByDescription.get(0)).isEqualTo(inProgressTask);

        assertThat(tasksByCaseInsensitive).hasSize(1);
        assertThat(tasksByCaseInsensitive.get(0)).isEqualTo(completedTask);
    }

    @Test
    @DisplayName("Should find tasks by partial search term")
    void shouldFindTasksByPartialSearchTerm() {
        // When - Search for partial term that appears in multiple tasks
        List<TaskEntity> tasksWithTask = taskRepository.findByTitleOrDescriptionContainingIgnoreCase("task");
        List<TaskEntity> tasksWithDescription = taskRepository.findByTitleOrDescriptionContainingIgnoreCase("desc");

        // Then
        assertThat(tasksWithTask).hasSize(4); // All tasks have "task" in title
        assertThat(tasksWithDescription).hasSize(4); // All tasks have "desc" in description
    }

    @Test
    @DisplayName("Should return empty list for non-matching search term")
    void shouldReturnEmptyListForNonMatchingSearchTerm() {
        // When
        List<TaskEntity> noMatches = taskRepository.findByTitleOrDescriptionContainingIgnoreCase("nonexistent");

        // Then
        assertThat(noMatches).isEmpty();
    }

    @Test
    @DisplayName("Should update task and maintain timestamps")
    void shouldUpdateTaskAndMaintainTimestamps() {
        // Given
        TaskEntity taskToUpdate = taskRepository.findById(pendingTask.getId()).orElseThrow();
        LocalDateTime originalCreatedDate = taskToUpdate.getCreatedDate();
        LocalDateTime originalUpdatedDate = taskToUpdate.getUpdatedDate();

        // When
        taskToUpdate.setTitle("Updated Title");
        taskToUpdate.setStatus(TaskStatus.IN_PROGRESS);
        TaskEntity updatedTask = taskRepository.save(taskToUpdate);
        entityManager.flush();

        // Then
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updatedTask.getCreatedDate()).isEqualTo(originalCreatedDate);
        assertThat(updatedTask.getUpdatedDate()).isAfter(originalUpdatedDate);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        // Given
        Long taskIdToDelete = pendingTask.getId();
        assertThat(taskRepository.existsById(taskIdToDelete)).isTrue();

        // When
        taskRepository.deleteById(taskIdToDelete);
        entityManager.flush();

        // Then
        assertThat(taskRepository.existsById(taskIdToDelete)).isFalse();
        assertThat(taskRepository.findById(taskIdToDelete)).isEmpty();
    }

    @Test
    @DisplayName("Should handle null values in optional fields")
    void shouldHandleNullValuesInOptionalFields() {
        // Given
        TaskEntity taskWithNulls = new TaskEntity("Required Title", null, TaskStatus.PENDING, null);

        // When
        TaskEntity savedTask = taskRepository.save(taskWithNulls);
        entityManager.flush();
        entityManager.clear();

        Optional<TaskEntity> retrievedTask = taskRepository.findById(savedTask.getId());

        // Then
        assertThat(retrievedTask).isPresent();
        assertThat(retrievedTask.get().getTitle()).isEqualTo("Required Title");
        assertThat(retrievedTask.get().getDescription()).isNull();
        assertThat(retrievedTask.get().getDueDate()).isNull();
        assertThat(retrievedTask.get().getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(retrievedTask.get().getCreatedDate()).isNotNull();
        assertThat(retrievedTask.get().getUpdatedDate()).isNotNull();
    }

    @Test
    @DisplayName("Should enforce database constraints")
    void shouldEnforceDatabaseConstraints() {
        // Given - Task without required title
        TaskEntity invalidTask = new TaskEntity();
        invalidTask.setDescription("Description without title");
        invalidTask.setStatus(TaskStatus.PENDING);

        // When & Then - This should fail due to nullable = false constraint on title
        try {
            taskRepository.save(invalidTask);
            entityManager.flush();
            // If we reach here, the constraint is not working as expected
            assert false : "Expected constraint violation for null title";
        } catch (Exception e) {
            // Expected - database constraint violation
            assertThat(e).isNotNull();
        }
    }
}*/