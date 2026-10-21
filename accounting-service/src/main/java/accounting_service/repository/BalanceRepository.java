package accounting_service.repository;

import accounting_service.data.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, UUID> {

    @Modifying
    @Query(value = """
    INSERT INTO balance(worker_id, amount)
    VALUES (:worker_id, 0)
    ON CONFLICT (worker_id) DO NOTHING
    """, nativeQuery = true)
    int createIfAbsent(@Param("worker_id") UUID id);
}
