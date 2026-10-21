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

import java.util.UUID;

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

    private Task newTaskWithoutExecutor;

    private UUID worker1uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private UUID worker2uuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {

        worker = workerRepository.save(
                Worker.builder()
                        .id(worker1uuid)
                        .name("Stas")
                        .email("stas@gmail.com")
                        .build()
        );

        worker2 = workerRepository.save(
                Worker.builder()
                        .id(worker2uuid)
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

        newTaskWithoutExecutor = taskRepository.save(
                Task.builder()
                        .title("No executor")
                        .description("Who should do this?")
                        .status(Status.NEW)
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
                // id генерирует последовательность, которая не сбрасывается между тестами,
                // поэтому сравниваем с реальным id, а не с константой 1
                .andExpect(jsonPath("$.content[0].id").value(taskNew.getId()))
                .andExpect(jsonPath("$.content[0].title").value("Need to be fixed"));
    }

    @Test
    @DisplayName("Пагинация: размер страницы больше 100 - 400")
    void getPage_shouldReturn400_whenSizeTooBig() throws Exception {
        mockMvc.perform(
                        get("/api/tasks")
                                .param("page", "0")
                                .param("size", "1000")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("size")));
    }

    @Test
    @DisplayName("Пагинация: отрицательная страница - 400")
    void getPage_shouldReturn400_whenPageNegative() throws Exception {
        mockMvc.perform(
                        get("/api/tasks")
                                .param("page", "-1")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание задачи без заголовка - 400")
    void create_shouldReturn400_whenTitleBlank() throws Exception {
        mockMvc.perform(
                        post("/api/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\": \"  \", \"description\": \"d\"}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.startsWith("title")));
    }

    @Test
    @DisplayName("Неизвестный статус - 400, а не 500")
    void setStatus_shouldReturn400_whenStatusUnknown() throws Exception {
        String template = String.format("/api/tasks/%d/status", taskNew.getId());
        mockMvc.perform(
                        patch(template)
                                .param("status", "DONE")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Отмена задачи без исполнителя - 200")
    void setStatus_shouldCancelTaskWithoutExecutor() throws Exception {
        String template = String.format("/api/tasks/%d/status", newTaskWithoutExecutor.getId());
        mockMvc.perform(
                        patch(template)
                                .param("status", "CANCELLED")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Назначение исполнителя завершённой задаче - 409")
    void setExecutor_shouldReturn409_whenTaskFinished() throws Exception {
        String template = String.format("/api/tasks/%d/executor", taskFinished.getId());
        mockMvc.perform(
                        patch(template)
                                .param("workerId", worker2.getId().toString())
                )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Получение задачи по id")
    void getById_shouldReturnTask() throws Exception {
        String template = String.format("/api/tasks/%d", taskNew.getId());
        mockMvc.perform(
                        get(template)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskNew.getId()))
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
                .andExpect(jsonPath("$.workerId").value(worker2.getId().toString()))
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

    @Test
    @DisplayName("Изменение статуса без исполнителя - ждём 409")
    void setStatus_shouldReturn409_whenTaskHasNoExecutor() throws Exception {
        String template = String.format("/api/tasks/%d/status", newTaskWithoutExecutor.getId());
        mockMvc.perform(
                        patch(template)
                                .param("status", "IN_PROGRESS")
                )
                .andExpect(status().isConflict());
    }
}
