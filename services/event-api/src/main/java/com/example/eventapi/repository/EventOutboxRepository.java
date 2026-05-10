package com.example.eventapi.repository;

import com.example.eventapi.domain.EventOutbox;
import com.example.eventapi.domain.OutboxStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventOutboxRepository {
  void insert(EventOutbox event);

  List<EventOutbox> findForUpdateBy(OutboxStatus status, int limit, Instant processingStartedBefore);

  List<EventOutbox> claimForUpdateBy(OutboxStatus status, int limit);

  Optional<EventOutbox> claimForUpdateBy(EventOutbox event);

  EventOutbox update(EventOutbox event);
}
