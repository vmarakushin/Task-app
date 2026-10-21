package task_service.repository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import task_service.data.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, UUID> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO workers (id, name, email)
        VALUES (:id, :name, :email)
        ON CONFLICT (id)
        DO UPDATE SET
            name = EXCLUDED.name,
            email = EXCLUDED.email
        WHERE
            workers.name IS DISTINCT FROM EXCLUDED.name
            OR workers.email IS DISTINCT FROM EXCLUDED.email
        """, nativeQuery = true)
    void upsert(
            @Param("id") UUID id,
            @Param("name") String name,
            @Param("email") String email
    );
}
