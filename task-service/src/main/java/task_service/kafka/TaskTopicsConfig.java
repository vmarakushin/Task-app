package task_service.kafka;

import events.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TaskTopicsConfig {

    @Bean
    public NewTopic taskFlowTopic() {
        return TopicBuilder.name(Topics.TASK_FLOW)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic taskStreamTopic() {
        return TopicBuilder.name(Topics.TASK_STREAM)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic taskAssignedTopic() {
        return TopicBuilder.name(Topics.TASK_ASSIGNED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic taskFinishedFlowTopic() {
        return TopicBuilder.name(Topics.TASK_FINISHED_FLOW)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
