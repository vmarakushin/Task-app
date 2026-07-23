package accounting_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AccountingTopicsConfig {

    @Bean
    public NewTopic paymentProcessedFlowTopic() {
        return TopicBuilder.name("payment-stream")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
