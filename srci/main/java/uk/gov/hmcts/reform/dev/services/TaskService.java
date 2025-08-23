package uk.gov.hmcts.reform.dev.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dto.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.model.Task;
import uk.gov.hmcts.reform.dev.model.TaskStatus;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    public TaskResponse createTask(CreateTaskRequest request) {
        Task task = new Task(
            request.getTitle(),
            request.getDescription(),
            request.getStatus(),
            request.getDueDateTime()
        );
        
        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }
    
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        return convertToResponse(task);
    }
    
    public List<TaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAllOrderByDueDate();
        return tasks.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        
        if (request.getTitle() != null) {
            existingTask.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingTask.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            existingTask.setStatus(request.getStatus());
        }
        if (request.getDueDateTime() != null) {
            existingTask.setDueDateTime(request.getDueDateTime());
        }
        
        Task updatedTask = taskRepository.save(existingTask);
        return convertToResponse(updatedTask);
    }
    
    public TaskResponse updateTaskStatus(Long id, TaskStatus status) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        
        existingTask.setStatus(status);
        Task updatedTask = taskRepository.save(existingTask);
        return convertToResponse(updatedTask);
    }
    
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task with id " + id + " not found");
        }
        taskRepository.deleteById(id);
    }
    
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        return tasks.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getDueDateTime(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}