package uk.gov.hmcts.reform.dev.services;

import uk.gov.hmcts.reform.dev.models.Task;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {
    private List<Task> tasks = new ArrayList<>();

    public Task createTask(Task task) {
        task.setId(UUID.randomUUID());
        tasks.add(task);
        return task;
    }

    public Task getTask(UUID id) {
        Optional<Task> task = tasks.stream().filter(t -> t.getId().equals(id)).findFirst();
        return task.orElse(null);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    public Task updateTaskStatus(UUID id, String status) {
        Task task = getTask(id);
        if (task != null) {
            task.setStatus(status);
        }
        return task;
    }

    public void deleteTask(UUID id) {
        tasks.removeIf(task -> task.getId().equals(id));
    }
}