package com.example.eventapi.service;

import com.example.eventapi.domain.EventOutbox;
import com.example.eventapi.domain.OutboxStatus;
import com.example.eventapi.repository.EventOutboxRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.example.eventapi.domain.OutboxStatus.*;
import static java.time.temporal.ChronoUnit.HOURS;

public record DefaultEventOutboxService(EventOutboxRepository eventOutboxRepository,
                                        int pendingEventLimit,
                                        int maxRetries) implements EventOutboxService {

  @Override
  public void create(EventOutbox event) {
    eventOutboxRepository.insert(event);
  }

  @Override
  public Optional<EventOutbox> claimEvent(EventOutbox event) {
    return eventOutboxRepository.claimForUpdateBy(event);
  }

  @Override
  public List<EventOutbox> claimPendingEvents() {
    return eventOutboxRepository.claimForUpdateBy(PENDING, pendingEventLimit);
  }

  @Override
  public List<EventOutbox> getStuckProcessingEvents() {
    return eventOutboxRepository
        .findForUpdateBy(PROCESSING, pendingEventLimit, Instant.now().minus(1, HOURS));
  }

  @Override
  public void updateFailed(EventOutbox event) {
    var retries = event.retries() + 1;
    var eventForUpdate = event
        .withRetries(retries)
        .withStatus(deriveStatus(retries));

    eventOutboxRepository.update(eventForUpdate);
  }

  @Override
  public void updateSent(EventOutbox event) {
    eventOutboxRepository.update(event.withStatus(SENT));
  }

  private OutboxStatus deriveStatus(int retries) {
    return retries >= maxRetries ? FAILED : PENDING;
  }
}
