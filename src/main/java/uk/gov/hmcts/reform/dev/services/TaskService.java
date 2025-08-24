package uk.gov.hmcts.reform.dev.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Task createTask(String title, String description, TaskStatus status, LocalDateTime dueDate) {
        Task task = new Task();
        task.setId(idGenerator.getAndIncrement());
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status != null ? status : TaskStatus.PENDING);
        task.setDueDate(dueDate);
        task.setCreatedDate(LocalDateTime.now());
        task.setUpdatedDate(LocalDateTime.now());
        
        tasks.add(task);
        return task;
    }

    public Optional<Task> getTaskById(Long id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public Optional<Task> updateTaskStatus(Long id, TaskStatus status) {
        Optional<Task> taskOpt = getTaskById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setStatus(status);
            task.setUpdatedDate(LocalDateTime.now());
            return Optional.of(task);
        }
        return Optional.empty();
    }

    public boolean deleteTask(Long id) {
        return tasks.removeIf(task -> task.getId().equals(id));
    }

    // Initialize with some sample data
    public void initializeSampleData() {
        if (tasks.isEmpty()) {
            createTask("Review case documents", "Review all submitted documents for case ABC123", 
                      TaskStatus.PENDING, LocalDateTime.now().plusDays(2));
            createTask("Schedule hearing", "Schedule hearing for case DEF456", 
                      TaskStatus.IN_PROGRESS, LocalDateTime.now().plusDays(5));
            createTask("Prepare case summary", "Prepare comprehensive case summary for review", 
                      TaskStatus.COMPLETED, LocalDateTime.now().minusDays(1));
        }
    }
}