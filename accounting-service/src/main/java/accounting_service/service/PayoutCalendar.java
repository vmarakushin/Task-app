package accounting_service.service;

import accounting_service.config.PaymentProperties;
import accounting_service.repository.PayoutScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * График выплат "через одну пятницу": от опорной даты каждые 14 дней.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutCalendar {

    static final int PERIOD_DAYS = 14;

    private final PayoutScheduleRepository repository;
    private final PaymentProperties properties;
    private final Clock clock;

    private volatile LocalDate anchor;

    @EventListener(ApplicationReadyEvent.class)
    public void logSchedule() {
        LocalDate today = LocalDate.now(clock);
        log.info("Payout anchor date: {}, next payday: {}", anchor(), nextPayday(today));
    }

    public boolean isPayday(LocalDate date) {
        long days = ChronoUnit.DAYS.between(anchor(), date);
        return days >= 0 && days % PERIOD_DAYS == 0;
    }

    /**
     * Ближайший день выплаты, начиная с from (включительно).
     */
    public LocalDate nextPayday(LocalDate from) {
        LocalDate first = anchor();
        if (!from.isAfter(first)) {
            return first;
        }
        long remainder = ChronoUnit.DAYS.between(first, from) % PERIOD_DAYS;
        return remainder == 0 ? from : from.plusDays(PERIOD_DAYS - remainder);
    }

    LocalDate anchor() {
        if (anchor == null) {
            anchor = resolveAnchor();
        }
        return anchor;
    }

    private LocalDate resolveAnchor() {
        String configured = properties.payout().anchorDate();

        if (configured != null && !configured.isBlank()) {
            LocalDate date = LocalDate.parse(configured.trim());
            if (date.getDayOfWeek() != DayOfWeek.FRIDAY) {
                log.warn("Configured payout anchor date {} is not a Friday", date);
            }
            return date;
        }

        // Ближайшая пятница строго после сегодняшнего дня: сегодняшние 00:00 уже прошли.
        LocalDate nextFriday = LocalDate.now(clock).with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        return repository.saveAnchorIfAbsent(nextFriday);
    }
}
