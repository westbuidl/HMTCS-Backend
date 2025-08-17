package main.java.uk.gov.hmcts.reform.dev.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    // Default constructor required by JPA
    public Task() {}

    // Parameterized constructor for creating a new task
    public Task(String title, String description, String status, LocalDateTime dueDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title; 
    }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        this.status = status; 
    }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { 
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        this.dueDate = dueDate; 
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', description='" + description + "', status='" + status + "', dueDate=" + dueDate + "}";
    }
}