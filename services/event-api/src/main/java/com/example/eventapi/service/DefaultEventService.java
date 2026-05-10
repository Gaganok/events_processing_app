package com.example.eventapi.service;

import com.example.eventapi.domain.Event;
import com.example.eventapi.repository.EventRepository;

public record DefaultEventService(EventRepository eventRepository) implements EventService {

  @Override
  public void create(Event event) {
    eventRepository.insert(event);
  }
}
