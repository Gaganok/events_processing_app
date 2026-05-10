package com.example.eventapi.repository;

import com.example.eventapi.domain.Event;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface EventRepository {
  void insert(Event event);

  void delete(UUID eventId);

  List<Event> findByIds(Set<UUID> eventIds);
}
