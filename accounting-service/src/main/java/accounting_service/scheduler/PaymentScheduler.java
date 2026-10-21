package accounting_service.scheduler;

import accounting_service.service.PaymentService;
import accounting_service.service.PayoutCalendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;
    private final PayoutCalendar payoutCalendar;
    private final Clock clock;

    @Scheduled(cron = "${payment.payout.cron}")
    @SchedulerLock(
            name = "payFinishedJobs",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT1M"
    )
    public void payFinishedJobs() {
        LocalDate today = LocalDate.now(clock);

        if (!payoutCalendar.isPayday(today)) {
            log.info("{} is not a payday, next payday: {}", today, payoutCalendar.nextPayday(today));
            return;
        }
        paymentService.processPayments();
    }

    @Scheduled(fixedDelayString = "${payment.outbox.interval-sec}", timeUnit = TimeUnit.SECONDS)
    public void sendPaymentEvents() {
        paymentService.sendPaymentsToKafka();
    }
}
