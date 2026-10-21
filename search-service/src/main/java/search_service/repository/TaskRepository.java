package search_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import search_service.data.dto.TaskResponse;
import search_service.data.model.Task;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** Работник подтягивается LEFT JOIN: задача возвращается, даже если user-stream ещё не обработан. */
    String SELECT_RESPONSE = """
            select new search_service.data.dto.TaskResponse(
                t.id, t.title, t.description, t.status, t.workerId, w.name, w.email)
            from Task t
            left join Worker w on w.id = t.workerId
            """;

    @Query(value = SELECT_RESPONSE, countQuery = "select count(t) from Task t")
    Page<TaskResponse> findResponses(Pageable pageable);

    @Query(SELECT_RESPONSE + " where t.id = :id")
    Optional<TaskResponse> findResponseById(@Param("id") Long id);

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
