package search_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import search_service.data.model.Worker;

import java.util.List;
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

    /** Нечёткий поиск pg_trgm, самые похожие первыми. */
    @Query(value = """
        SELECT *
        FROM workers w
        WHERE w.email % :email
        ORDER BY similarity(w.email, :email) DESC
        LIMIT 100
        """, nativeQuery = true)
    List<Worker> fuzzySearchByEmail(@Param("email") String email);
}
