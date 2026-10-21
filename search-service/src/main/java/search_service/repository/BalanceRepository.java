package search_service.repository;

import search_service.data.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface BalanceRepository extends JpaRepository <Balance, UUID> {

    @Modifying
    @Query(value = """
    INSERT INTO balance(worker_id, amount)
    VALUES (:worker_id, 0)
    ON CONFLICT (worker_id) DO NOTHING
    """, nativeQuery = true)
    int createIfAbsent(@Param("worker_id") UUID id);

    /**
     * Прибавляет сумму к балансу. Если баланса ещё нет (user-flow не обработан),
     * создаёт его с этой суммой - порядок событий не важен.
     */
    @Modifying
    @Query(value = """
    INSERT INTO balance(worker_id, amount)
    VALUES (:worker_id, :amount)
    ON CONFLICT (worker_id) DO UPDATE
        SET amount = balance.amount + EXCLUDED.amount
    """, nativeQuery = true)
    int credit(@Param("worker_id") UUID workerId, @Param("amount") BigDecimal amount);
}
