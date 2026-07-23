package search_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import search_service.data.model.Task;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO tasks (id, title, description, status, worker_id)
        VALUES (:id, :title, :description, :status, :worker_id)
        ON CONFLICT (id) DO UPDATE SET
            title = EXCLUDED.title,
            description = EXCLUDED.description,
            status = EXCLUDED.status,
            worker_id = EXCLUDED.worker_id
        """, nativeQuery = true)
    void upsert(
            @Param("id") Long id,
            @Param("title") String title,
            @Param("description") String description,
            @Param("status") String status,
            @Param("worker_id") UUID workerId
    );
}
