package uk.gov.hmcts.reform.dev.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dev.entities.TaskEntity;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    /**
     * Create a new task
     */
    public Task createTask(String title, String description, TaskStatus status, LocalDateTime dueDate) {
        log.info("Creating new task with title: {}", title);
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        
        TaskEntity entity = taskMapper.createEntity(title, description, status, dueDate);
        TaskEntity savedEntity = taskRepository.save(entity);
        
        log.info("Task created successfully with ID: {}", savedEntity.getId());
        return taskMapper.toModel(savedEntity);
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        log.debug("Fetching task with ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        Optional<TaskEntity> entity = taskRepository.findById(id);
        return entity.map(taskMapper::toModel);
    }

    /**
     * Get all tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        log.debug("Fetching all tasks");
        
        List<TaskEntity> entities = taskRepository.findAllByOrderByDueDateAsc();
        return entities.stream()
                .map(taskMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by status
     */
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(TaskStatus status) {
        log.debug("Fetching tasks with status: {}", status);
        
        if (status == null) {
            throw new IllegalArgumentException("Task status cannot be null");
        }
        
        List<TaskEntity> entities = taskRepository.findByStatusOrderByDueDateAsc(status);
        return entities.stream()
                .map(taskMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Update task status
     */
    public Optional<Task> updateTaskStatus(Long id, TaskStatus status) {
        log.info("Updating task {} status to: {}", id, status);
        
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Task status cannot be null");
        }
        
        Optional<TaskEntity> entityOpt = taskRepository.findById(id);
        if (entityOpt.isPresent()) {
            TaskEntity entity = entityOpt.get();
            entity.setStatus(status);
            TaskEntity savedEntity = taskRepository.save(entity);
            log.info("Task {} status updated successfully", id);
            return Optional.of(taskMapper.toModel(savedEntity));
        }
        
        log.warn("Task with ID {} not found for status update", id);
        return Optional.empty();
    }

    /**
     * Update entire task
     */
    public Optional<Task> updateTask(Long id, String title, String description, 
                                   TaskStatus status, LocalDateTime dueDate) {
        log.info("Updating task with ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        
        Optional<TaskEntity> entityOpt = taskRepository.findById(id);
        if (entityOpt.isPresent()) {
            TaskEntity entity = entityOpt.get();
            entity.setTitle(title);
            entity.setDescription(description);
            if (status != null) {
                entity.setStatus(status);
            }
            entity.setDueDate(dueDate);
            
            TaskEntity savedEntity = taskRepository.save(entity);
            log.info("Task {} updated successfully", id);
            return Optional.of(taskMapper.toModel(savedEntity));
        }
        
        log.warn("Task with ID {} not found for update", id);
        return Optional.empty();
    }

    /**
     * Delete task
     */
    public boolean deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            log.info("Task {} deleted successfully", id);
            return true;
        }
        
        log.warn("Task with ID {} not found for deletion", id);
        return false;
    }

    /**
     * Get overdue tasks
     */
    @Transactional(readOnly = true)
    public List<Task> getOverdueTasks() {
        log.debug("Fetching overdue tasks");
        
        LocalDateTime now = LocalDateTime.now();
        List<TaskStatus> excludedStatuses = Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED);
        
        List<TaskEntity> entities = taskRepository.findOverdueTasks(now, excludedStatuses);
        return entities.stream()
                .map(taskMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Get task statistics
     */
    @Transactional(readOnly = true)
    public TaskStatistics getTaskStatistics() {
        log.debug("Calculating task statistics");
        
        long totalTasks = taskRepository.count();
        long pendingTasks = taskRepository.countByStatus(TaskStatus.PENDING);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long cancelledTasks = taskRepository.countByStatus(TaskStatus.CANCELLED);
        long overdueTasks = getOverdueTasks().size();
        
        return new TaskStatistics(totalTasks, pendingTasks, inProgressTasks, 
                                completedTasks, cancelledTasks, overdueTasks);
    }

    /**
     * Search tasks by title or description
     */
    @Transactional(readOnly = true)
    public List<Task> searchTasks(String searchTerm) {
        log.debug("Searching tasks with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllTasks();
        }
        
        List<TaskEntity> entities = taskRepository.findByTitleOrDescriptionContainingIgnoreCase(searchTerm);
        return entities.stream()
                .map(taskMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Initialize sample data for development/testing
     */
    public void initializeSampleData() {
        log.info("Checking if sample data initialization is needed");
        
        if (taskRepository.count() == 0) {
            log.info("Initializing sample data");
            
            createTask("Review case documents", 
                      "Review all submitted documents for case ABC123", 
                      TaskStatus.PENDING, 
                      LocalDateTime.now().plusDays(2));
                      
            createTask("Schedule hearing", 
                      "Schedule hearing for case DEF456", 
                      TaskStatus.IN_PROGRESS, 
                      LocalDateTime.now().plusDays(5));
                      
            createTask("Prepare case summary", 
                      "Prepare comprehensive case summary for review", 
                      TaskStatus.COMPLETED, 
                      LocalDateTime.now().minusDays(1));
                      
            createTask("File legal documents", 
                      "File required legal documents for case GHI789", 
                      TaskStatus.PENDING, 
                      LocalDateTime.now().minusDays(1)); // This will be overdue
            
            log.info("Sample data initialized successfully");
        } else {
            log.debug("Sample data already exists, skipping initialization");
        }
    }

    /**
     * Inner class for task statistics
     */
    public static class TaskStatistics {
        private final long totalTasks;
        private final long pendingTasks;
        private final long inProgressTasks;
        private final long completedTasks;
        private final long cancelledTasks;
        private final long overdueTasks;

        public TaskStatistics(long totalTasks, long pendingTasks, long inProgressTasks, 
                            long completedTasks, long cancelledTasks, long overdueTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.inProgressTasks = inProgressTasks;
            this.completedTasks = completedTasks;
            this.cancelledTasks = cancelledTasks;
            this.overdueTasks = overdueTasks;
        }

        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getPendingTasks() { return pendingTasks; }
        public long getInProgressTasks() { return inProgressTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getCancelledTasks() { return cancelledTasks; }
        public long getOverdueTasks() { return overdueTasks; }
    }
}