package task_service.service;

import task_service.data.dto.*;
import task_service.data.model.Status;
import task_service.data.model.Task;
import task_service.data.model.Worker;
import task_service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task_service.repository.TaskRepository;
import task_service.repository.WorkerRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final WorkerRepository workerRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getPage(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

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
                new TaskCreatedFlowEvent(saved.getId(), Instant.now())
        );
        eventPublisher.publishEvent(
                new TaskStreamEvent(saved.getId(), saved.getTitle(), saved.getExecutor(), saved.getDescription(), saved.getStatus())
        );

        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse setExecutor(Long taskId, Long executorId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(
                        Messages.TASK_NOT_FOUND.format(taskId)
                ));

        Worker newWorker = workerRepository.findById(executorId)
                .orElseThrow(() -> new NotFoundException(
                        Messages.WORKER_NOT_FOUND.format(executorId)
                ));

        task.setExecutor(newWorker);

        eventPublisher.publishEvent(
                new WorkerAssignedEvent(task.getId(), newWorker.getId())
        );
        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse setStatus(Long taskId, Status status) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(
                        Messages.TASK_NOT_FOUND.format(taskId)
                ));

        if (!task.getStatus().canMoveTo(status)) {
            throw new IllegalStateException(
                    Messages.ILLEGAL_TASK_STATUS_SEQUENCE.format(taskId, status, task.getStatus())
            );
        }

        task.setStatus(status);
        return mapToResponse(task);
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
