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

    @Modifying
    @Query(value = """
        UPDATE job 
        SET finished = true
        WHERE task_id = :task_id
        AND finished = FALSE
        """, nativeQuery = true)
    int finishIfNotFinished(@Param("task_id") Long taskId);
}
