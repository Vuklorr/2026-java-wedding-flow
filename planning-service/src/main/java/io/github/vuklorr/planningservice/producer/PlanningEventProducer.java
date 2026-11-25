package io.github.vuklorr.planningservice.producer;

import io.github.vuklorr.planningservice.event.TaskCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlanningEventProducer {

    private final static String NAME_PLANNING_TOPIC = "planning.events";

    private final KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate;

    public PlanningEventProducer(KafkaTemplate<String, TaskCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishTaskCreated(TaskCreatedEvent event) {
        kafkaTemplate.send(NAME_PLANNING_TOPIC, String.valueOf(event.weddingId()), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish event: {}",
                                event.eventId(),
                                exception
                        );
                    } else {
                        log.info(
                                "Event published: eventId={}, topic={}, partition={}, offset={}",
                                event.eventId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    }
                });
    }
}
