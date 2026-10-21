package search_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import search_service.data.dto.TaskResponse;
import search_service.exception.NotFoundException;
import search_service.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        return taskRepository.findResponses(pageable)
                .map(this::withPlaceholders);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(long id) {
        return taskRepository.findResponseById(id)
                .map(this::withPlaceholders)
                .orElseThrow(() -> new NotFoundException(
                        Messages.TASK_NOT_FOUND.format(id)
                ));
    }

    private TaskResponse withPlaceholders(TaskResponse task) {
        if (task.workerId() == null) {
            return copyWithWorker(task, Messages.NO_WORKER.text());
        }
        if (task.workerName() == null) {
            return copyWithWorker(task, Messages.WORKER_NOT_SYNCED.text());
        }
        return task;
    }

    private static TaskResponse copyWithWorker(TaskResponse task, String placeholder) {
        return new TaskResponse(
                task.id(),
                task.title(),
                task.description(),
                task.status(),
                task.workerId(),
                placeholder,
                placeholder
        );
    }
}
