package com.example.eventapi.service;

import com.example.eventapi.domain.Event;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface EventService {

  void create(Event event);

  List<Event> getEventsBy(Set<UUID> eventIds);
}
