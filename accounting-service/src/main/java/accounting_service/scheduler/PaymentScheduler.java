package accounting_service.scheduler;

import accounting_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    @Scheduled(cron = "${payment.cron}")
    @SchedulerLock(
            name = "payFinishedJobs",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT1M"
    )
    public void payFinishedJobs() {
        paymentService.processPayments();
    }

    @Scheduled(fixedDelayString = "${payment.outbox.interval-sec}")
    public void sendPaymentEvents() {
        paymentService.sendPaymentsToKafka();
    }
}
