package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.dev.models.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.models.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskController Unit Tests")
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private ObjectMapper objectMapper;
    private Task sampleTask;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        testDateTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Sample Task");
        sampleTask.setDescription("Sample Description");
        sampleTask.setStatus(TaskStatus.PENDING);
        sampleTask.setDueDate(testDateTime.plusDays(1));
        sampleTask.setCreatedDate(testDateTime);
        sampleTask.setUpdatedDate(testDateTime);
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTaskSuccessfully() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setStatus(TaskStatus.PENDING);
        request.setDueDate(testDateTime.plusDays(2));

        Task createdTask = new Task();
        createdTask.setId(2L);
        createdTask.setTitle("New Task");
        createdTask.setDescription("New Description");
        createdTask.setStatus(TaskStatus.PENDING);

        when(taskService.createTask(
                eq("New Task"),
                eq("New Description"),
                eq(TaskStatus.PENDING),
                any(LocalDateTime.class)
        )).thenReturn(createdTask);

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.description", is("New Description")))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(taskService).createTask("New Task", "New Description", TaskStatus.PENDING, request.getDueDate());
    }

    @Test
    @DisplayName("Should return bad request when creating task with null title")
    void shouldReturnBadRequestWhenCreatingTaskWithNullTitle() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle(null);
        request.setDescription("Description");
        request.setStatus(TaskStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(anyString(), anyString(), any(TaskStatus.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void shouldGetTaskByIdSuccessfully() throws Exception {
        // Given
        Long taskId = 1L;
        when(taskService.getTaskById(taskId)).thenReturn(Optional.of(sampleTask));

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Sample Task")))
                .andExpect(jsonPath("$.description", is("Sample Description")))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(taskService).getTaskById(taskId);
    }

    @Test
    @DisplayName("Should get all tasks successfully")
    void shouldGetAllTasksSuccessfully() throws Exception {
        // Given
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Task 2")));

        verify(taskService).getAllTasks();
    }

    @Test
    @DisplayName("Should update task status successfully")
    void shouldUpdateTaskStatusSuccessfully() throws Exception {
        // Given
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.IN_PROGRESS;
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(newStatus);

        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setStatus(newStatus);

        when(taskService.updateTaskStatus(taskId, newStatus)).thenReturn(Optional.of(updatedTask));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(taskService).updateTaskStatus(taskId, newStatus);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() throws Exception {
        // Given
        Long taskId = 1L;
        when(taskService.deleteTask(taskId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId);
    }
}