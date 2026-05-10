package com.example.eventapi.service;

import com.example.eventapi.domain.Event;
import com.example.eventapi.repository.EventRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DefaultEventService(EventRepository eventRepository) implements EventService {

  @Override
  public void create(Event event) {
    eventRepository.insert(event);
  }

  @Override
  public List<Event> getEventsBy(Set<UUID> eventIds) {
    return eventRepository.findByIds(eventIds);
  }
}
