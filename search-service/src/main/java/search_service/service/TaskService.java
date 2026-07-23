package search_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import search_service.data.dto.TaskResponse;
import search_service.exception.NotFoundException;
import search_service.data.model.Task;
import search_service.data.model.Worker;

import search_service.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

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
