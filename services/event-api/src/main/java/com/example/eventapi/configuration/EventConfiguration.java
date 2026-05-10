package com.example.eventapi.configuration;

import com.example.eventapi.domain.EventMessage;
import com.example.eventapi.repository.EventOutboxRepository;
import com.example.eventapi.repository.EventRepository;
import com.example.eventapi.repository.PgEventOutboxRepository;
import com.example.eventapi.repository.PgEventRepository;
import com.example.eventapi.service.BrokerAwareEventService;
import com.example.eventapi.service.DefaultEventOutboxService;
import com.example.eventapi.service.DefaultEventService;
import com.example.eventapi.service.EventOutboxService;
import com.example.eventapi.service.EventService;
import com.example.eventapi.service.OutboxDispatcher;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventConfiguration {

  @Bean
  public EventService eventService(EventRepository eventRepository,
                                   EventOutboxService eventOutboxService,
                                   ApplicationEventPublisher publisher) {
    var defaultEventService = new DefaultEventService(eventRepository);
    return new BrokerAwareEventService(defaultEventService, eventOutboxService, publisher);
  }

  @Bean
  public OutboxDispatcher outboxDispatcher(EventOutboxService eventOutboxService,
                                           EventService eventService,
                                           KafkaTemplate<String, EventMessage> kafkaTemplate) {
    return new OutboxDispatcher(eventOutboxService, eventService, kafkaTemplate);
  }

  @Bean
  public EventRepository eventRepository(DSLContext dsl) {
    return new PgEventRepository(dsl);
  }

  @Bean
  public EventOutboxRepository eventOutboxRepository(DSLContext dsl) {
    return new PgEventOutboxRepository(dsl);
  }

  @Bean
  public EventOutboxService eventOutboxService(EventOutboxRepository eventOutboxRepository,
                                               @Value("${event.outbox.pending-event-limit}") int pendingEventLimit,
                                               @Value("${event.outbox.max-retries}") int maxRetries) {
    return new DefaultEventOutboxService(eventOutboxRepository, pendingEventLimit, maxRetries);
  }
}
