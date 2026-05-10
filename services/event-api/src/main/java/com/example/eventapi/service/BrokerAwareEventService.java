package com.example.eventapi.service;

import com.example.eventapi.domain.Event;
import com.example.eventapi.domain.EventMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

public record BrokerAwareEventService(EventService delegate,
                                      KafkaTemplate<String, EventMessage> kafkaTemplate) implements EventService {

  private static final String TOPIC = "events";

  @Override
  @Transactional
  public void create(Event event) {
    delegate.create(event);
    kafkaTemplate.send(TOPIC, event.id().toString(), EventMessage.from(event));
  }
}
