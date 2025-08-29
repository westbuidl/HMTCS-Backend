package uk.gov.hmcts.reform.dev.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.dev.models.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.models.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "logging.level.org.hibernate.SQL=ERROR",
    "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=ERROR"
})
@Transactional
@DisplayName("Task Management Integration Tests")
class TaskManagementIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        // Clear database before each test
        taskRepository.deleteAll();
        taskRepository.flush(); // Ensure deletion is committed
    }

    @Test
    @DisplayName("Should create task via API successfully")
    void shouldCreateTaskViaApiSuccessfully() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Integration Test Task");
        request.setDescription("Testing task creation via API");
        request.setStatus(TaskStatus.PENDING);
        request.setDueDate(LocalDateTime.now().plusDays(1));

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print()) // Add debug output
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Integration Test Task")))
                .andExpect(jsonPath("$.description", is("Testing task creation via API")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.createdDate", notNullValue()))
                .andExpect(jsonPath("$.updatedDate", notNullValue()));
    }

    @Test
    @DisplayName("Should return bad request when creating task with empty title")
    void shouldReturnBadRequestWhenCreatingTaskWithEmptyTitle() throws Exception {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("");
        request.setDescription("Testing validation");
        request.setStatus(TaskStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all tasks")
    void shouldGetAllTasks() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("Should get task by ID successfully")
    void shouldGetTaskByIdSuccessfully() throws Exception {
        // Given - Create a task first
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Test Task for ID");
        request.setDescription("Testing get by ID");
        request.setStatus(TaskStatus.PENDING);

        String requestJson = objectMapper.writeValueAsString(request);

        String response = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        var taskResponse = objectMapper.readTree(response);
        Long taskId = taskResponse.get("id").asLong();

        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.intValue())))
                .andExpect(jsonPath("$.title", is("Test Task for ID")))
                .andExpect(jsonPath("$.description", is("Testing get by ID")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("Should return not found when getting non-existent task")
    void shouldReturnNotFoundWhenGettingNonExistentTask() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tasks/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update task status successfully")
    void shouldUpdateTaskStatusSuccessfully() throws Exception {
        // Given - Create a task first
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Task for Status Update");
        createRequest.setDescription("Testing status update");
        createRequest.setStatus(TaskStatus.PENDING);

        String createRequestJson = objectMapper.writeValueAsString(createRequest);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var taskResponse = objectMapper.readTree(createResponse);
        Long taskId = taskResponse.get("id").asLong();

        // Prepare update request
        UpdateTaskStatusRequest updateRequest = new UpdateTaskStatusRequest();
        updateRequest.setStatus(TaskStatus.IN_PROGRESS);
        String updateRequestJson = objectMapper.writeValueAsString(updateRequest);

        // When & Then
        mockMvc.perform(put("/api/tasks/{id}/status", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(taskId.intValue())))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.updatedDate", notNullValue()));
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTaskSuccessfully() throws Exception {
        // Given - Create a task first
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Task for Deletion");
        createRequest.setDescription("Testing deletion");
        createRequest.setStatus(TaskStatus.PENDING);

        String createRequestJson = objectMapper.writeValueAsString(createRequest);

        String createResponse = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var taskResponse = objectMapper.readTree(createResponse);
        Long taskId = taskResponse.get("id").asLong();

        // When & Then
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle CORS headers correctly")
    void shouldHandleCorsHeadersCorrectly() throws Exception {
        // When & Then
        mockMvc.perform(options("/api/tasks")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }
}