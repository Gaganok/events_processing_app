package com.example.eventapi.service;

import com.example.eventapi.domain.Event;
import com.example.eventapi.domain.EventMessage;
import com.example.eventapi.domain.EventOutbox;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

public class OutboxDispatcher {

  private final EventOutboxService outboxService;
  private final EventService eventService;
  private final KafkaTemplate<String, EventMessage> kafkaTemplate;

  private static final String TOPIC = "events";

  public OutboxDispatcher(EventOutboxService outboxService, EventService eventService, KafkaTemplate<String, EventMessage> kafkaTemplate) {
    this.outboxService = outboxService;
    this.eventService = eventService;
    this.kafkaTemplate = kafkaTemplate;
  }

  @TransactionalEventListener(phase = AFTER_COMMIT)
  public void handle(OutboxPublishEvent publishEvent) {
    outboxService.claimEvent(publishEvent.outbox())
        .ifPresent(outbox -> sendEvent(publishEvent.event(), outbox));
  }

  @Transactional
  @Scheduled(fixedDelayString = "${outbox.retry.interval:30000}")
  public void retryFailed() {
    var claimedOutboxes = outboxService.claimPendingEvents();

    if (claimedOutboxes.isEmpty()) return;

    var outboxMap = claimedOutboxes.stream()
        .collect(toMap(EventOutbox::id, identity()));

    eventService.getEventsBy(outboxMap.keySet())
        .forEach(event -> sendEvent(event, outboxMap.get(event.id())));
  }

  @Transactional
  @Scheduled(fixedDelayString = "${outbox.stuck.interval:60000}")
  public void recoverStuckEvents() {
    outboxService.getStuckProcessingEvents().forEach(outboxService::updateFailed);
  }

  private void sendEvent(Event event, EventOutbox outbox) {
    kafkaTemplate.send(TOPIC, event.id().toString(), EventMessage.from(event))
        .whenCompleteAsync((result, ex) -> {
          if (ex == null) {
            outboxService.updateSent(outbox);
          } else {
            outboxService.updateFailed(outbox);
          }
        });
  }

  public record OutboxPublishEvent(Event event, EventOutbox outbox) {
  }
}
