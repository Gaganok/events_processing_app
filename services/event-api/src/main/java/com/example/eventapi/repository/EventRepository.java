package com.example.eventapi.repository;

import com.example.eventapi.domain.Event;

public interface EventRepository {
  void insert(Event event);
}
