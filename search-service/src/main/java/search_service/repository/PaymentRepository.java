package search_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import search_service.data.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO inbox_payment_events (id, task_id, worker_id, amount, processed, created_at)
        VALUES (:id, :taskId, :workerId, :amount, :processed, :createdAt)
        ON CONFLICT (id) DO NOTHING
        """, nativeQuery = true)
    int saveIfNotExists(
            @Param("id") Long id,
            @Param("taskId") Long taskId,
            @Param("workerId") UUID workerId,
            @Param("amount") BigDecimal amount,
            @Param("processed") Boolean processed,
            @Param("createdAt") Instant createdAt
    );
}
