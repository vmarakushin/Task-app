package accounting_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class PayoutScheduleRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Сохраняет опорную дату, только если она ещё не сохранена,
     * и возвращает ту, что в итоге лежит в БД.
     * Так перезапуск сервиса (или второй инстанс) не сдвигает график.
     */
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
