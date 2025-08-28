package uk.gov.hmcts.reform.dev.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.entities.TaskEntity;
import uk.gov.hmcts.reform.dev.mappers.TaskMapper;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private TaskEntity sampleEntity;
    private Task sampleTask;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        sampleEntity = new TaskEntity();
        sampleEntity.setId(1L);
        sampleEntity.setTitle("Test Task");
        sampleEntity.setDescription("Test Description");
        sampleEntity.setStatus(TaskStatus.PENDING);
        sampleEntity.setDueDate(testDateTime.plusDays(1));
        sampleEntity.setCreatedDate(testDateTime);
        sampleEntity.setUpdatedDate(testDateTime);

        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Test Task");
        sampleTask.setDescription("Test Description");
        sampleTask.setStatus(TaskStatus.PENDING);
        sampleTask.setDueDate(testDateTime.plusDays(1));
        sampleTask.setCreatedDate(testDateTime);
        sampleTask.setUpdatedDate(testDateTime);
    }

    @Test
    @DisplayName("Should create task successfully with valid data")
    void shouldCreateTaskSuccessfully() {
        // Given
        String title = "New Task";
        String description = "New Description";
        TaskStatus status = TaskStatus.PENDING;
        LocalDateTime dueDate = testDateTime.plusDays(2);

        TaskEntity newEntity = new TaskEntity(title, description, status, dueDate);
        TaskEntity savedEntity = new TaskEntity(title, description, status, dueDate);
        savedEntity.setId(2L);

        Task expectedTask = new Task();
        expectedTask.setId(2L);
        expectedTask.setTitle(title);

        when(taskMapper.createEntity(title, description, status, dueDate)).thenReturn(newEntity);
        when(taskRepository.save(newEntity)).thenReturn(savedEntity);
        when(taskMapper.toModel(savedEntity)).thenReturn(expectedTask);

        // When
        Task result = taskService.createTask(title, description, status, dueDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo(title);

        verify(taskMapper).createEntity(title, description, status, dueDate);
        verify(taskRepository).save(newEntity);
        verify(taskMapper).toModel(savedEntity);
    }

    @Test
    @DisplayName("Should throw exception when creating task with null title")
    void shouldThrowExceptionWhenCreatingTaskWithNullTitle() {
        // Given
        String nullTitle = null;
        String description = "Description";
        TaskStatus status = TaskStatus.PENDING;
        LocalDateTime dueDate = testDateTime.plusDays(1);

        // When & Then
        assertThatThrownBy(() -> taskService.createTask(nullTitle, description, status, dueDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title cannot be null or empty");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void shouldGetTaskByIdSuccessfully() {
        // Given
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleEntity));
        when(taskMapper.toModel(sampleEntity)).thenReturn(sampleTask);

        // When
        Optional<Task> result = taskService.getTaskById(taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(taskId);
        assertThat(result.get().getTitle()).isEqualTo("Test Task");

        verify(taskRepository).findById(taskId);
        verify(taskMapper).toModel(sampleEntity);
    }

    @Test
    @DisplayName("Should return empty when task not found by ID")
    void shouldReturnEmptyWhenTaskNotFoundById() {
        // Given
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // When
        Optional<Task> result = taskService.getTaskById(taskId);

        // Then
        assertThat(result).isEmpty();
        verify(taskRepository).findById(taskId);
        verify(taskMapper, never()).toModel(any());
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() {
        // Given
        List<TaskEntity> entities = Arrays.asList(sampleEntity);

        when(taskRepository.findAllByOrderByDueDateAsc()).thenReturn(entities);
        when(taskMapper.toModel(sampleEntity)).thenReturn(sampleTask);

        // When
        List<Task> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(sampleTask);

        verify(taskRepository).findAllByOrderByDueDateAsc();
        verify(taskMapper).toModel(sampleEntity);
    }

    @Test
    @DisplayName("Should update task status successfully")
    void shouldUpdateTaskStatusSuccessfully() {
        // Given
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.IN_PROGRESS;
        TaskEntity updatedEntity = new TaskEntity();
        updatedEntity.setId(taskId);
        updatedEntity.setStatus(newStatus);
        
        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setStatus(newStatus);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleEntity));
        when(taskRepository.save(sampleEntity)).thenReturn(updatedEntity);
        when(taskMapper.toModel(updatedEntity)).thenReturn(updatedTask);

        // When
        Optional<Task> result = taskService.updateTaskStatus(taskId, newStatus);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(newStatus);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(sampleEntity);
        verify(taskMapper).toModel(updatedEntity);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() {
        // Given
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);

        // When
        boolean result = taskService.deleteTask(taskId);

        // Then
        assertThat(result).isTrue();
        verify(taskRepository).existsById(taskId);
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("Should get overdue tasks successfully")
    void shouldGetOverdueTasksSuccessfully() {
        // Given
        TaskEntity overdueEntity = new TaskEntity();
        overdueEntity.setId(2L);
        overdueEntity.setDueDate(testDateTime.minusDays(1));
        overdueEntity.setStatus(TaskStatus.PENDING);

        Task overdueTask = new Task();
        overdueTask.setId(2L);
        overdueTask.setStatus(TaskStatus.PENDING);

        List<TaskEntity> overdueEntities = Arrays.asList(overdueEntity);
        
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class), anyList())).thenReturn(overdueEntities);
        when(taskMapper.toModel(overdueEntity)).thenReturn(overdueTask);

        // When
        List<Task> result = taskService.getOverdueTasks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);

        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class), anyList());
        verify(taskMapper).toModel(overdueEntity);
    }

    @Test
    @DisplayName("Should initialize sample data when repository is empty")
    void shouldInitializeSampleDataWhenRepositoryIsEmpty() {
        // Given
        when(taskRepository.count()).thenReturn(0L);
        when(taskMapper.createEntity(anyString(), anyString(), any(TaskStatus.class), any(LocalDateTime.class)))
                .thenReturn(sampleEntity);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(sampleEntity);
        when(taskMapper.toModel(any(TaskEntity.class))).thenReturn(sampleTask);

        // When
        taskService.initializeSampleData();

        // Then
        verify(taskRepository).count();
        verify(taskMapper, times(4)).createEntity(anyString(), anyString(), any(TaskStatus.class), any(LocalDateTime.class));
        verify(taskRepository, times(4)).save(any(TaskEntity.class));
    }

    @Test
    @DisplayName("Should not initialize sample data when repository has data")
    void shouldNotInitializeSampleDataWhenRepositoryHasData() {
        // Given
        when(taskRepository.count()).thenReturn(5L);

        // When
        taskService.initializeSampleData();

        // Then
        verify(taskRepository).count();
        verify(taskMapper, never()).createEntity(anyString(), anyString(), any(TaskStatus.class), any(LocalDateTime.class));
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }
}