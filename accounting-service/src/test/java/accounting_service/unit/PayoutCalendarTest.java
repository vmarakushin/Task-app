package accounting_service.unit;

import accounting_service.config.PaymentProperties;
import accounting_service.repository.PayoutScheduleRepository;
import accounting_service.service.PayoutCalendar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PayoutCalendarTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Yerevan");

    private static final LocalDate WEDNESDAY = LocalDate.of(2026, 10, 21);
    private static final LocalDate FRIDAY = LocalDate.of(2026, 10, 23);

    private final PayoutScheduleRepository repository = mock(PayoutScheduleRepository.class);

    @Test
    @DisplayName("Без настройки первая выплата - ближайшая пятница после старта")
    void anchor_shouldBeNextFriday() {
        PayoutCalendar calendar = calendar(WEDNESDAY.atTime(15, 0), null);

        assertEquals(FRIDAY, calendar.nextPayday(WEDNESDAY));
    }

    @Test
    @DisplayName("Старт в пятницу днём - первая выплата через неделю")
    void anchor_shouldSkipTodayIfStartedOnFriday() {
        PayoutCalendar calendar = calendar(FRIDAY.atTime(10, 0), null);

        assertEquals(FRIDAY.plusWeeks(1), calendar.nextPayday(FRIDAY));
    }

    @Test
    @DisplayName("Выплата через одну пятницу")
    void isPayday_shouldBeEveryOtherFriday() {
        PayoutCalendar calendar = calendar(WEDNESDAY.atTime(15, 0), null);

        assertFalse(calendar.isPayday(WEDNESDAY));
        assertTrue(calendar.isPayday(FRIDAY));
        assertFalse(calendar.isPayday(FRIDAY.plusWeeks(1)));
        assertTrue(calendar.isPayday(FRIDAY.plusWeeks(2)));
        assertFalse(calendar.isPayday(FRIDAY.plusWeeks(3)));
        assertTrue(calendar.isPayday(FRIDAY.plusWeeks(52)));
        assertFalse(calendar.isPayday(FRIDAY.minusWeeks(2)));
    }

    @Test
    @DisplayName("Следующая выплата считается от любой даты")
    void nextPayday_shouldRoundUpToPeriod() {
        PayoutCalendar calendar = calendar(WEDNESDAY.atTime(15, 0), null);

        assertEquals(FRIDAY, calendar.nextPayday(FRIDAY));
        assertEquals(FRIDAY.plusWeeks(2), calendar.nextPayday(FRIDAY.plusDays(1)));
        assertEquals(FRIDAY.plusWeeks(2), calendar.nextPayday(FRIDAY.plusWeeks(1)));
    }

    @Test
    @DisplayName("Дата из настроек важнее сохранённой в БД")
    void anchor_shouldUseConfiguredDate() {
        LocalDate configured = LocalDate.of(2026, 10, 30);
        PayoutCalendar calendar = calendar(WEDNESDAY.atTime(15, 0), configured.toString());

        assertTrue(calendar.isPayday(configured));
        assertFalse(calendar.isPayday(FRIDAY));
        verify(repository, never()).saveAnchorIfAbsent(any());
    }

    @Test
    @DisplayName("Сохранённая в БД дата не сдвигается при перезапуске")
    void anchor_shouldComeFromDatabase() {
        LocalDate saved = LocalDate.of(2026, 10, 9);
        PayoutCalendar calendar = calendar(WEDNESDAY.atTime(15, 0), null, saved);

        assertTrue(calendar.isPayday(LocalDate.of(2026, 10, 23)));
        assertEquals(LocalDate.of(2026, 10, 23), calendar.nextPayday(WEDNESDAY));
        verify(repository).saveAnchorIfAbsent(FRIDAY);
    }

    private PayoutCalendar calendar(LocalDateTime now, String configuredAnchor) {
        when(repository.saveAnchorIfAbsent(any())).thenAnswer(inv -> inv.getArgument(0));
        return build(now, configuredAnchor);
    }

    private PayoutCalendar calendar(LocalDateTime now, String configuredAnchor, LocalDate alreadySaved) {
        when(repository.saveAnchorIfAbsent(any())).thenReturn(alreadySaved);
        return build(now, configuredAnchor);
    }

    private PayoutCalendar build(LocalDateTime now, String configuredAnchor) {
        Clock clock = Clock.fixed(now.atZone(ZONE).toInstant(), ZONE);
        PaymentProperties properties = new PaymentProperties(
                new BigDecimal("1000.00"),
                new PaymentProperties.Payout("0 0 0 * * FRI", configuredAnchor),
                new PaymentProperties.Outbox(100, 10)
        );
        return new PayoutCalendar(repository, properties, clock);
    }
}
