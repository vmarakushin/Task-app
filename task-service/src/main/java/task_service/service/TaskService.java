package task_service.service;

import events.task.TaskCreatedEvent;
import events.task.TaskFinishedEvent;
import events.task.TaskStatus;
import events.task.TaskStreamEvent;
import events.task.WorkerAssignedEvent;
import task_service.data.dto.TaskCreateRequest;
import task_service.data.dto.TaskResponse;
import task_service.data.model.Status;
import task_service.data.model.Task;
import task_service.data.model.Worker;
import task_service.exception.IllegalStatusSequenceException;
import task_service.exception.NoWorkerException;
import task_service.exception.NotFoundException;
import task_service.exception.TerminalTaskException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task_service.repository.TaskRepository;
import task_service.repository.WorkerRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final WorkerRepository workerRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getPage(int page, int size) {

        // Без сортировки порядок строк в PostgreSQL не гарантирован: страницы могут "плавать".
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(long id) {
        return taskRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new NotFoundException(
                        Messages.TASK_NOT_FOUND.format(id)
                ));
    }

    @Transactional
    public TaskResponse create(TaskCreateRequest request) {

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(Status.NEW)
                .build();

        Task saved = taskRepository.save(task);

        eventPublisher.publishEvent(
                new TaskCreatedEvent(saved.getId(), Instant.now())
        );
        eventPublisher.publishEvent(toStreamEvent(saved));

        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse setExecutor(Long taskId, UUID executorId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(
                        Messages.TASK_NOT_FOUND.format(taskId)
                ));

        if (task.getStatus().isTerminal()) {
            throw new TerminalTaskException(
                    Messages.WORKER_CANNOT_BE_ASSIGNED_TO_CLOSED_TASK.format(executorId, taskId, task.getStatus())
            );
        }

        Worker newWorker = workerRepository.findById(executorId)
                .orElseThrow(() -> new NotFoundException(
                        Messages.WORKER_NOT_FOUND.format(executorId)
                ));

        task.setExecutor(newWorker);

        eventPublisher.publishEvent(
                new WorkerAssignedEvent(task.getId(), task.getExecutor().getId())
        );
        eventPublisher.publishEvent(toStreamEvent(task));
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse setStatus(Long taskId, Status status) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(
                        Messages.TASK_NOT_FOUND.format(taskId)
                ));

        if (!task.getStatus().canMoveTo(status)) {
            throw new IllegalStatusSequenceException(
                    Messages.ILLEGAL_TASK_STATUS_SEQUENCE.format(taskId, status, task.getStatus())
            );
        }

        if (status.requiresWorker() && task.getExecutor() == null) {
            throw new NoWorkerException(
                    Messages.WORKER_SHOULD_BE_ASSIGNED.format(status)
            );
        }

        task.setStatus(status);

        if (task.getStatus() == Status.FINISHED) {
            eventPublisher.publishEvent(
                    new TaskFinishedEvent(task.getId(), task.getExecutor().getId(), Instant.now())
            );
        }
        eventPublisher.publishEvent(toStreamEvent(task));
        return mapToResponse(task);
    }

    private TaskStreamEvent toStreamEvent(Task task) {

        Worker worker = task.getExecutor();

        return new TaskStreamEvent(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                TaskStatus.valueOf(task.getStatus().name()),
                worker != null ? worker.getId() : null,
                worker != null ? worker.getName() : null,
                worker != null ? worker.getEmail() : null
        );
    }

    private TaskResponse mapToResponse(Task task) {

        Worker worker = task.getExecutor();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                worker != null ? worker.getId() : null,
                worker != null ? worker.getName() : Messages.NO_WORKER.text(),
                worker != null ? worker.getEmail() : Messages.NO_WORKER.text()
        );
    }
}
