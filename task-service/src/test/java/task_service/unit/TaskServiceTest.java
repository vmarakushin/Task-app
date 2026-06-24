package task_service.unit;

import org.junit.jupiter.api.DisplayName;
import task_service.data.dto.TaskCreateRequest;
import task_service.data.dto.TaskCreatedFlowEvent;
import task_service.data.dto.TaskResponse;
import task_service.data.dto.WorkerAssignedEvent;
import task_service.data.model.Status;
import task_service.data.model.Task;
import task_service.data.model.Worker;
import task_service.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import task_service.repository.TaskRepository;
import task_service.repository.WorkerRepository;
import task_service.service.TaskService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private WorkerRepository workerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("Получение страницы - успех")
    void getPage_shouldReturnPage() {

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .build();

        Page<Task> page = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<TaskResponse> result = taskService.getPage(0, 10);

        assertEquals(1, result.getTotalElements());

        TaskResponse response = result.getContent().getFirst();

        assertEquals(task.getId(), response.id());
        assertEquals(task.getTitle(), response.title());

        verify(taskRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Получение по ID - успех")
    void findById_shouldReturnTask() {

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse result = taskService.findById(1L);

        assertEquals(task.getId(), result.id());
        assertEquals(task.getTitle(), result.title());

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение задачи по ID - не найдено")
    void findById_shouldThrowWhenTaskNotFound() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.findById(1L)
        );

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Создание задачи - успех")
    void create_shouldCreateTaskAndPublishEvent() {

        TaskCreateRequest request = new TaskCreateRequest(
                "Task",
                "Description"
        );

        Task savedTask = Task.builder()
                .id(1L)
                .title(request.title())
                .description(request.description())
                .status(Status.NEW)
                .build();

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        TaskResponse result = taskService.create(request);

        assertEquals(savedTask.getId(), result.id());
        assertEquals(savedTask.getTitle(), result.title());
        assertEquals(Status.NEW, result.status());

        verify(taskRepository).save(any(Task.class));

        ArgumentCaptor<TaskCreatedFlowEvent> captor =
                ArgumentCaptor.forClass(TaskCreatedFlowEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        TaskCreatedFlowEvent event = captor.getValue();

        assertEquals(savedTask.getId(), event.id());
    }

    @Test
    @DisplayName("Изменение исполнителя задачи - успех")
    void setExecutor_shouldAssignWorker() {

        Worker worker = Worker.builder()
                .id(10L)
                .name("Vlad")
                .email("vlad@test.com")
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(workerRepository.findById(10L))
                .thenReturn(Optional.of(worker));

        TaskResponse result = taskService.setExecutor(1L, 10L);

        assertEquals(worker.getId(), result.workerId());
        assertEquals(worker.getName(), result.workerName());

        verify(taskRepository).findById(1L);
        verify(workerRepository).findById(10L);

        ArgumentCaptor<WorkerAssignedEvent> captor =
                ArgumentCaptor.forClass(WorkerAssignedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());

        WorkerAssignedEvent event = captor.getValue();

        assertEquals(task.getId(), event.taskId());
        assertEquals(worker.getId(), event.workerId());
    }

    @Test
    @DisplayName("Изменение исполнителя задачи - задача не найдена")
    void setExecutor_shouldThrowWhenTaskNotFound() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.setExecutor(1L, 10L)
        );

        verify(taskRepository).findById(1L);

        verify(workerRepository, never())
                .findById(anyLong());
    }

    @Test
    @DisplayName("Изменение исполнителя задачи - исполнитель не найден")
    void setExecutor_shouldThrowWhenWorkerNotFound() {

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(workerRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.setExecutor(1L, 10L)
        );

        verify(taskRepository).findById(1L);
        verify(workerRepository).findById(10L);
    }

    @Test
    @DisplayName("Обновление статуса задачи - успех")
    void setStatus_shouldUpdateStatus() {

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse result =
                taskService.setStatus(1L, Status.IN_PROGRESS);

        assertEquals(Status.IN_PROGRESS, result.status());

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Изменение статуса задачи - задача не найдена")
    void setStatus_shouldThrowWhenTaskNotFound() {

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.setStatus(1L, Status.IN_PROGRESS)
        );

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Изменение статуса задачи - неправильный порядок статусов")
    void setStatus_shouldThrowWhenTransitionIsInvalid() {

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.FINISHED)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        assertThrows(
                IllegalStateException.class,
                () -> taskService.setStatus(1L, Status.IN_PROGRESS)
        );

        verify(taskRepository).findById(1L);
    }
}
