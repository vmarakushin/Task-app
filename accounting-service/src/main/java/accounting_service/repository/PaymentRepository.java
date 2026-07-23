package accounting_service.repository;

import accounting_service.data.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(
            value = "SELECT pay_finished_jobs(:paymentAmount)",
            nativeQuery = true
    )
    Integer createPayments(
            @Param("paymentAmount")
            BigDecimal paymentAmount
    );

    @Query(value = """
        SELECT *
        FROM payment
        WHERE processed = FALSE
        ORDER BY created_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED;
        """, nativeQuery = true)
    List<Payment> findTop100UnprocessedOrderByCreatedAtAsc(@Param("limit") int limit);

    @Modifying
    @Query(value = """
        UPDATE payment
        SET processed = TRUE
        WHERE id IN (:ids)
    """, nativeQuery = true)
    int markProcessed(@Param("ids") List<Long> ids);

    boolean existsByProcessedFalse();
}
