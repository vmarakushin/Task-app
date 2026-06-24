package task_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import task_service.AbstractIntegrationTest;
import task_service.data.dto.TaskCreateRequest;
import task_service.data.model.Status;
import task_service.data.model.Task;
import task_service.data.model.Worker;
import task_service.repository.TaskRepository;
import task_service.repository.WorkerRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TaskControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private TaskRepository taskRepository;

    private Worker worker;

    private Worker worker2;

    private Task taskNew;

    private Task taskFinished;

    @BeforeEach
    void setUp() {

        worker = workerRepository.save(
                Worker.builder()
                        .name("Stas")
                        .email("stas@gmail.com")
                        .build()
        );

        worker2 = workerRepository.save(
                Worker.builder()
                        .name("Andrey")
                        .email("andrey@gmail.com")
                        .build()
        );

        taskNew = taskRepository.save(
                Task.builder()
                        .title("Need to be fixed")
                        .description("Exactly this stuff must be fixed")
                        .status(Status.NEW)
                        .executor(worker)
                        .build()
        );

        taskFinished = taskRepository.save(
                Task.builder()
                        .title("Another title")
                        .description("Another description")
                        .status(Status.FINISHED)
                        .executor(worker)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        workerRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение задач с пагинацией")
    void getPage_shouldReturnPage() throws Exception {
        mockMvc.perform(
                        get("/api/tasks")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Need to be fixed"));
    }

    @Test
    @DisplayName("Получение задачи по id")
    void getById_shouldReturnTask() throws Exception {
        String template = String.format("/api/tasks/%d", worker.getId());
        mockMvc.perform(
                        get(template)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(worker.getId()))
                .andExpect(jsonPath("$.title").value("Need to be fixed"));
    }

    @Test
    @DisplayName("Получение несуществующей задачи")
    void getById_shouldReturn404() throws Exception {
        mockMvc.perform(
                        get("/api/tasks/999")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Создание задачи")
    void create_shouldReturnCreatedTask() throws Exception {
        TaskCreateRequest request =
                new TaskCreateRequest(
                        "Task title",
                        "Task description"
                );

        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task title"))
                .andExpect(jsonPath("$.description").value("Task description"));
    }

    @Test
    @DisplayName("Назначение исполнителя")
    void setExecutor_shouldAssignWorker() throws Exception {
        String template = String.format("/api/tasks/%d/executor", taskNew.getId());
        mockMvc.perform(
                        patch(template)
                                .param("workerId", worker2.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerId").value(worker2.getId()))
                .andExpect(jsonPath("$.workerName").value("Andrey"));
    }

    @Test
    @DisplayName("Изменение статуса")
    void setStatus_shouldUpdateStatus() throws Exception {
        String template = String.format("/api/tasks/%d/status", taskNew.getId());
        mockMvc.perform(
                        patch(template)
                                .param("status", "IN_PROGRESS")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.title").value("Need to be fixed"));
    }
}
