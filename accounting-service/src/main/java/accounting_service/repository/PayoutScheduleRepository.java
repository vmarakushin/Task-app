package accounting_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class PayoutScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    /** Сохраняет опорную дату, если её ещё нет, и возвращает сохранённую. */
    public LocalDate saveAnchorIfAbsent(LocalDate candidate) {
        jdbcTemplate.update("""
                INSERT INTO payout_schedule (id, anchor_date)
                VALUES (1, ?)
                ON CONFLICT (id) DO NOTHING
                """, candidate);

        return jdbcTemplate.queryForObject(
                "SELECT anchor_date FROM payout_schedule WHERE id = 1",
                LocalDate.class
        );
    }
}
