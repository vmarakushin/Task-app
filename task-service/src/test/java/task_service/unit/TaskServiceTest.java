package task_service.unit;

import events.task.TaskCreatedEvent;
import events.task.TaskStreamEvent;
import events.task.WorkerAssignedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import task_service.data.dto.TaskCreateRequest;
import task_service.data.dto.TaskResponse;
import task_service.data.model.Status;
import task_service.data.model.Task;
import task_service.data.model.Worker;
import task_service.exception.IllegalStatusSequenceException;
import task_service.exception.NoWorkerException;
import task_service.exception.NotFoundException;
import task_service.exception.TerminalTaskException;
import task_service.repository.TaskRepository;
import task_service.repository.WorkerRepository;
import task_service.service.TaskService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        TaskCreatedEvent event = findEvent(captor, TaskCreatedEvent.class);

        assertEquals(savedTask.getId(), event.taskId());
        assertEquals(savedTask.getId(), findEvent(captor, TaskStreamEvent.class).taskId());
    }

    @Test
    @DisplayName("Изменение исполнителя задачи - успех")
    void setExecutor_shouldAssignWorker() {
        UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Worker worker = Worker.builder()
                .id(workerId)
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

        when(workerRepository.findById(workerId))
                .thenReturn(Optional.of(worker));

        TaskResponse result = taskService.setExecutor(1L, workerId);

        assertEquals(worker.getId(), result.workerId());
        assertEquals(worker.getName(), result.workerName());

        verify(taskRepository).findById(1L);
        verify(workerRepository).findById(workerId);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        WorkerAssignedEvent event = findEvent(captor, WorkerAssignedEvent.class);
        assertEquals(worker.getId(), findEvent(captor, TaskStreamEvent.class).workerId());

        assertEquals(task.getId(), event.taskId());
        assertEquals(worker.getId(), event.workerId());
    }

    @Test
    @DisplayName("Изменение исполнителя задачи - задача не найдена")
    void setExecutor_shouldThrowWhenTaskNotFound() {
        UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.setExecutor(1L, workerId)
        );

        verify(taskRepository).findById(1L);

        verify(workerRepository, never())
                .findById(any(UUID.class));
    }

    @Test
    @DisplayName("Изменение исполнителя задачи - исполнитель не найден")
    void setExecutor_shouldThrowWhenWorkerNotFound() {
        UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(workerRepository.findById(workerId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.setExecutor(1L, workerId)
        );

        verify(taskRepository).findById(1L);
        verify(workerRepository).findById(workerId);
    }

    @Test
    @DisplayName("Обновление статуса задачи - успех")
    void setStatus_shouldUpdateStatus() {
        UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Worker worker = Worker.builder()
                .id(workerId)
                .email("user@mail.com")
                .name("User")
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.NEW)
                .executor(worker)
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
        UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Worker worker = Worker.builder()
                .id(workerId)
                .email("user@mail.com")
                .name("User")
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(Status.FINISHED)
                .executor(worker)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        assertThrows(
                IllegalStatusSequenceException.class,
                () -> taskService.setStatus(1L, Status.IN_PROGRESS)
        );

        verify(taskRepository).findById(1L);
    }

    @Test
    @DisplayName("Изменение статуса задачи - отсутствие исполнителя")
    void setStatus_shouldThrow_ifWorkerNotAssigned() {
        Task task = Task.builder()
                .id(1L)
                .title("No executor")
                .description("Who should do this?")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        assertThrows(
                NoWorkerException.class,
                () -> taskService.setStatus(
                        1L,
                        Status.IN_PROGRESS
                )
        );
    }

    @Test
    @DisplayName("Отмена новой задачи без исполнителя - разрешена")
    void setStatus_shouldCancelTaskWithoutWorker() {
        Task task = Task.builder()
                .id(1L)
                .title("Not needed anymore")
                .status(Status.NEW)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse result = taskService.setStatus(1L, Status.CANCELLED);

        assertEquals(Status.CANCELLED, result.status());
    }

    @Test
    @DisplayName("Назначение исполнителя отменённой задаче - запрещено")
    void setExecutor_shouldThrow_whenTaskCancelled() {
        UUID workerId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Task task = Task.builder()
                .id(1L)
                .title("Cancelled")
                .status(Status.CANCELLED)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        assertThrows(
                TerminalTaskException.class,
                () -> taskService.setExecutor(1L, workerId)
        );

        verify(workerRepository, never()).findById(any());
    }

    private static <T> T findEvent(ArgumentCaptor<Object> captor, Class<T> type) {
        return captor.getAllValues().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event not published: " + type.getSimpleName()));
    }
}
