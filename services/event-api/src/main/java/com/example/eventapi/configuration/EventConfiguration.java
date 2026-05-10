package com.example.eventapi.configuration;

import com.example.eventapi.domain.EventMessage;
import com.example.eventapi.repository.EventRepository;
import com.example.eventapi.repository.PgEventRepository;
import com.example.eventapi.service.BrokerAwareEventService;
import com.example.eventapi.service.DefaultEventService;
import com.example.eventapi.service.EventService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class EventConfiguration {

  @Bean
  public EventService eventService(EventRepository eventRepository,
                                   KafkaTemplate<String, EventMessage> kafkaTemplate) {
    var defaultEventService = new DefaultEventService(eventRepository);
    return new BrokerAwareEventService(defaultEventService, kafkaTemplate);
  }

  @Bean
  public EventRepository eventRepository(DSLContext dsl) {
    return new PgEventRepository(dsl);
  }
}
