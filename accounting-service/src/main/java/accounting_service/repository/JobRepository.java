package accounting_service.repository;

import accounting_service.data.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO job (task_id, worker_id)
        VALUES (:task_id, :worker_id)
        ON CONFLICT (task_id)
        DO UPDATE SET
            worker_id = EXCLUDED.worker_id
        WHERE
            job.worker_id IS DISTINCT FROM EXCLUDED.worker_id
        AND
            job.finished = FALSE
        """, nativeQuery = true)
    int upsert(
            @Param("task_id") Long taskId,
            @Param("worker_id") UUID workerId
    );

    /**
     * Помечает job завершённым. Если task-assigned ещё не обработан и job нет,
     * создаёт его сразу завершённым - исполнитель берётся из TaskFinishedEvent.
     * Уже завершённый job не трогает (повторное событие ничего не меняет).
     */
    @Modifying
    @Query(value = """
        INSERT INTO job (task_id, worker_id, finished)
        VALUES (:task_id, :worker_id, TRUE)
        ON CONFLICT (task_id) DO UPDATE SET
            finished = TRUE,
            worker_id = EXCLUDED.worker_id
        WHERE
            job.finished = FALSE
        """, nativeQuery = true)
    int finish(
            @Param("task_id") Long taskId,
            @Param("worker_id") UUID workerId
    );
}
