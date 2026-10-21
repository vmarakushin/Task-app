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

    /**
     * @return 1 - событие новое, 0 - уже было обработано раньше (дубль)
     */
    @Modifying
    @Query(value = """
        INSERT INTO inbox_payment_events (payment_id, task_id, worker_id, amount, created_at)
        VALUES (:paymentId, :taskId, :workerId, :amount, :createdAt)
        ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    int saveIfNotExists(
            @Param("paymentId") Long paymentId,
            @Param("taskId") Long taskId,
            @Param("workerId") UUID workerId,
            @Param("amount") BigDecimal amount,
            @Param("createdAt") Instant createdAt
    );
}
