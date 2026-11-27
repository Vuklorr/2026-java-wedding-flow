package io.github.vuklorr.planningservice.listener;

import io.github.vuklorr.planningservice.event.TaskCreatedEvent;
import io.github.vuklorr.planningservice.producer.PlanningEventProducer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskCreatedEventListener {
    private final PlanningEventProducer planningEventProducer;

    public TaskCreatedEventListener(PlanningEventProducer planningEventProducer) {
        this.planningEventProducer = planningEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        planningEventProducer.publishTaskCreated(event);
    }
}
