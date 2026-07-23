package search_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import search_service.data.dto.TaskResponse;
import search_service.service.TaskService;

@RestController
@RequestMapping("/api/search/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "API получения задач")
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
}
