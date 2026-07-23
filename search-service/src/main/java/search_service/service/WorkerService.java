package search_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import search_service.data.dto.WorkerResponse;
import search_service.data.model.Worker;
import search_service.exception.NotFoundException;
import search_service.repository.WorkerRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;

    @Transactional(readOnly = true)
    public Page<WorkerResponse> getPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return workerRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public WorkerResponse findById(UUID id) {
        return workerRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new NotFoundException(
                        Messages.WORKER_NOT_FOUND.format(id)
                ));
    }

    @Transactional(readOnly = true)
    public List<WorkerResponse> fuzzySearchByEmail(String emailQuery) {
        if (emailQuery == null || emailQuery.isBlank()) {
            return List.of();
        }
        return workerRepository.fuzzySearchByEmail(emailQuery.trim().toLowerCase())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WorkerResponse mapToResponse(Worker worker) {
        return new WorkerResponse(worker.getId(), worker.getName(), worker.getEmail());
    }
}
