package task_service.service;

import auth_service.event.user.UserStreamEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import task_service.repository.WorkerRepository;

@Service
@RequiredArgsConstructor
public class TaskKafkaHandler {

    private final WorkerRepository workerRepository;

    public void handleUserStream(UserStreamEvent event) {
        workerRepository.upsert(event.id(), event.firstName(), event.email());
    }
}
