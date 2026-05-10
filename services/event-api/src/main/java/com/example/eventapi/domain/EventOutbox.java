package com.example.eventapi.domain;

import java.time.Instant;
import java.util.UUID;

import static com.example.eventapi.domain.OutboxStatus.PENDING;
import static java.time.Instant.now;


public record EventOutbox(UUID id, OutboxStatus status, int retries, Instant createdAt) {
  public static EventOutbox from(Event event) {
    return new EventOutbox(event.id(), PENDING, 0, now());
  }

  public EventOutbox withStatus(OutboxStatus status) {
    return new EventOutbox(id, status, retries, createdAt);
  }

  public EventOutbox withRetries(int retries) {
    return new EventOutbox(id, status, retries, createdAt);
  }
}
