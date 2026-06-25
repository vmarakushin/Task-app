package task_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TaskTopicsConfig {

    @Bean
    public NewTopic taskFlowTopic() {
        return TopicBuilder.name("task-flow")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic taskStreamTopic() {
        return TopicBuilder.name("task-stream")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic taskAssignedTopic() {
        return TopicBuilder.name("task-assigned")
                .partitions(1)
                .replicas(1)
                .build();
    }
}