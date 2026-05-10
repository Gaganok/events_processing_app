package com.example.eventapi.service;

import com.example.eventapi.domain.EventOutbox;

import java.util.List;
import java.util.Optional;

public interface EventOutboxService {
  void create(EventOutbox event);

  Optional<EventOutbox> claimEvent(EventOutbox event);

  List<EventOutbox> claimPendingEvents();

  List<EventOutbox> getStuckProcessingEvents();

  void updateFailed(EventOutbox event);

  void updateSent(EventOutbox event);
}
