package uk.gov.hmcts.reform.dev.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.dev.entities.TaskEntity;
import uk.gov.hmcts.reform.dev.models.Task;

@Component
public class TaskMapper {
    
    /**
     * Convert TaskEntity to Task model
     */
    public Task toModel(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Task task = new Task();
        task.setId(entity.getId());
        task.setTitle(entity.getTitle());
        task.setDescription(entity.getDescription());
        task.setStatus(entity.getStatus());
        task.setDueDate(entity.getDueDate());
        task.setCreatedDate(entity.getCreatedDate());
        task.setUpdatedDate(entity.getUpdatedDate());
        
        return task;
    }
    
    /**
     * Convert Task model to TaskEntity
     */
    public TaskEntity toEntity(Task task) {
        if (task == null) {
            return null;
        }
        
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setStatus(task.getStatus());
        entity.setDueDate(task.getDueDate());
        entity.setCreatedDate(task.getCreatedDate());
        entity.setUpdatedDate(task.getUpdatedDate());
        
        return entity;
    }
    
    /**
     * Create TaskEntity from creation parameters
     */
    public TaskEntity createEntity(String title, String description, 
                                  uk.gov.hmcts.reform.dev.models.TaskStatus status, 
                                  java.time.LocalDateTime dueDate) {
        return new TaskEntity(title, description, status, dueDate);
    }
}