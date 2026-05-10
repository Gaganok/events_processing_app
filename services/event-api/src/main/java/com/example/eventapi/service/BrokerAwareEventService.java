package com.example.eventapi.service;

import com.example.eventapi.domain.Event;
import com.example.eventapi.domain.EventOutbox;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BrokerAwareEventService implements EventService {

  private final EventService delegate;
  private final EventOutboxService outboxService;
  private final ApplicationEventPublisher publisher;

  public BrokerAwareEventService(EventService delegate, EventOutboxService outboxService, ApplicationEventPublisher publisher) {
    this.delegate = delegate;
    this.outboxService = outboxService;
    this.publisher = publisher;
  }

  @Override
  @Transactional
  public void create(Event event) {
    delegate.create(event);

    var outbox = EventOutbox.from(event);
    outboxService.create(outbox);

    publisher.publishEvent(new OutboxDispatcher.OutboxPublishEvent(event, outbox));
  }

  @Override
  public List<Event> getEventsBy(Set<UUID> eventIds) {
    return delegate.getEventsBy(eventIds);
  }

  @Override
  public void delete(UUID eventId) {
    delegate.delete(eventId);
  }
}
