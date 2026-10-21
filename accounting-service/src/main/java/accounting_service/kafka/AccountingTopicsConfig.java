package accounting_service.kafka;

import events.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AccountingTopicsConfig {

    @Bean
    public NewTopic paymentProcessedFlowTopic() {
        return TopicBuilder.name(Topics.PAYMENT_STREAM)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
