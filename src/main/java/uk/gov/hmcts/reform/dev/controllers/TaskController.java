package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.dev.models.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.services.TaskService;

import jakarta.annotation.PostConstruct;  
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*") 
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostConstruct
    public void init() {
        taskService.initializeSampleData();
    }

    @PostMapping(produces = "application/json", consumes = "application/json")
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        try {
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Task task = taskService.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getDueDate()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskService.getTaskById(id);
        return task.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping(value = "/{id}/status", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable Long id, 
            @RequestBody UpdateTaskStatusRequest request) {
        try {
            if (request.getStatus() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            Optional<Task> updatedTask = taskService.updateTaskStatus(id, request.getStatus());
            return updatedTask.map(ResponseEntity::ok)
                             .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        return deleted ? ResponseEntity.noContent().build() 
                      : ResponseEntity.notFound().build();
    }
}