package com.example.eventapi.service;


import com.example.eventapi.domain.EventOutbox;
import com.example.eventapi.repository.EventOutboxRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.eventapi.domain.OutboxStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DefaultEventOutboxServiceTest {

  private final EventOutboxRepository eventOutboxRepository = mock();
  private final DefaultEventOutboxService underTest = new DefaultEventOutboxService(eventOutboxRepository, 10, 3);

  @Test
  void shouldInsertEventOutbox() {
    var outbox = anEventOutbox();
    underTest.create(outbox);
    verify(eventOutboxRepository).insert(outbox);
  }

  @Test
  void shouldClaimEvent() {
    var outbox = anEventOutbox();
    when(eventOutboxRepository.claimForUpdateBy(outbox)).thenReturn(Optional.of(outbox));

    var result = underTest.claimEvent(outbox);

    assertThat(result).contains(outbox);
  }

  @Test
  void shouldClaimPendingEvents() {
    var events = List.of(anEventOutbox());
    when(eventOutboxRepository.claimForUpdateBy(PENDING, 10)).thenReturn(events);

    var result = underTest.claimPendingEvents();

    assertThat(result).isEqualTo(events);
  }

  @Test
  void shouldGetStuckProcessingEvents() {
    var events = List.of(anEventOutbox());
    when(eventOutboxRepository.findForUpdateBy(eq(PROCESSING), eq(10), any(Instant.class))).thenReturn(events);

    var result = underTest.getStuckProcessingEvents();

    assertThat(result).isEqualTo(events);
  }

  @Test
  void shouldUpdateFailedEventWithRetries() {
    var outbox = anEventOutbox().withRetries(1);
    underTest.updateFailed(outbox);

    var captor = ArgumentCaptor.forClass(EventOutbox.class);
    verify(eventOutboxRepository).update(captor.capture());
    assertThat(captor.getValue().status()).isEqualTo(PENDING);
  }

  @Test
  void shouldSetStatusToFailedWhenMaxRetriesExceeded() {
    var outbox = anEventOutbox().withRetries(3);
    underTest.updateFailed(outbox);

    var captor = ArgumentCaptor.forClass(EventOutbox.class);
    verify(eventOutboxRepository).update(captor.capture());
    assertThat(captor.getValue().status()).isEqualTo(FAILED);
  }

  @Test
  void shouldUpdateEventStatusToSent() {
    var outbox = spy(anEventOutbox());
    underTest.updateSent(outbox);

    verify(outbox).withStatus(SENT);
  }

  private EventOutbox anEventOutbox() {
    return new EventOutbox(UUID.randomUUID(), PENDING, 0, Instant.now());
  }
}