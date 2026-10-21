package task_service.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import task_service.AbstractIntegrationTest;
import task_service.data.model.Status;
import task_service.data.model.Task;
import task_service.repository.TaskRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Два запроса прочитали одну и ту же задачу, первый сохранил изменения,
 * второй пытается сохранить свои поверх устаревшей версии.
 */
@SpringBootTest
@ActiveProfiles("test")
public class OptimisticLockIT extends AbstractIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
    }

    @Test
    @DisplayName("Сохранение устаревшей версии задачи отклоняется")
    void staleUpdate_shouldFail() {
        Task saved = taskRepository.save(Task.builder()
                .title("Task")
                .status(Status.NEW)
                .build());

        // Вне транзакции каждый findById - отдельная сессия: это два независимых "запроса".
        Task firstRequest = taskRepository.findById(saved.getId()).orElseThrow();
        Task secondRequest = taskRepository.findById(saved.getId()).orElseThrow();

        firstRequest.setStatus(Status.CANCELLED);
        taskRepository.save(firstRequest);

        secondRequest.setStatus(Status.IN_PROGRESS);
        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> taskRepository.save(secondRequest));

        Task actual = taskRepository.findById(saved.getId()).orElseThrow();
        assertEquals(Status.CANCELLED, actual.getStatus());
        assertEquals(1L, actual.getVersion());
    }
}
