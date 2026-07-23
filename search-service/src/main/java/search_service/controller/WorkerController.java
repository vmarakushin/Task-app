package search_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import search_service.data.dto.WorkerResponse;
import search_service.service.WorkerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search/workers")
@RequiredArgsConstructor
@Tag(name = "Workers", description = "API получения работников")
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping
    @Operation(summary = "Получить страницу работников")
    public ResponseEntity<Page<WorkerResponse>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(workerService.getPage(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить работника по ID")
    public ResponseEntity<WorkerResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(workerService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Нечеткий поиск работников по email")
    public ResponseEntity<List<WorkerResponse>> fuzzySearchByEmail(@RequestParam String query) {
        return ResponseEntity.ok(workerService.fuzzySearchByEmail(query));
    }
}
