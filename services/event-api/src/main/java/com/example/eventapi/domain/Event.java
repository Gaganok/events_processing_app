package com.example.eventapi.domain;

import java.time.Instant;
import java.util.UUID;

import static com.example.eventapi.domain.EventStatus.RECEIVED;
import static java.util.UUID.randomUUID;

public record Event(UUID id, String payload, EventStatus status, Instant createdAt) {
  public static Event from(String payload) {
    return new Event(randomUUID(), payload, RECEIVED, Instant.now());
  }
}
