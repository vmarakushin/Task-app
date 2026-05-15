package task_application.controller;

import task_application.data.dto.TaskCreateRequest;
import task_application.data.dto.TaskResponse;
import task_application.data.model.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import task_application.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "API управления задачами")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Получить задачи с пагинацией")
    public Page<TaskResponse> getPage(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return taskService.getPage(page, size);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Получить задачу по id")
    public TaskResponse getById(@PathVariable Long taskId) {
        return taskService.findById(taskId);
    }

    @PostMapping
    @Operation(summary = "Создать задачу")
    public TaskResponse create(@RequestBody TaskCreateRequest request) {
        return taskService.create(request);
    }

    @PatchMapping("/{taskId}/executor")
    @Operation(summary = "Назначить исполнителя задачи")
    public TaskResponse setExecutor(
            @PathVariable Long taskId,
            @RequestParam Long workerId
    ) {
        return taskService.setExecutor(taskId, workerId);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Сменить статус задачи")
    public TaskResponse setStatus(
            @PathVariable Long taskId,
            @RequestParam Status status
    ) {
        return taskService.setStatus(taskId, status);
    }
}
