package com.example.eventapi.repository;

import com.example.eventapi.domain.EventOutbox;
import com.example.eventapi.domain.OutboxStatus;
import org.jooq.DSLContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.example.eventapi.domain.OutboxStatus.PENDING;
import static com.example.eventapi.domain.OutboxStatus.PROCESSING;
import static com.example.eventapi.repository.Schema.EVENT_OUTBOX;

public record PgEventOutboxRepository(DSLContext dsl) implements EventOutboxRepository {

  @Override
  public void insert(EventOutbox outbox) {
    dsl.insertInto(EVENT_OUTBOX.TABLE)
        .set(EVENT_OUTBOX.EVENT_ID, outbox.id())
        .set(EVENT_OUTBOX.STATUS, outbox.status())
        .set(EVENT_OUTBOX.RETRIES, outbox.retries())
        .set(EVENT_OUTBOX.CREATED_AT, outbox.createdAt())
        .execute();
  }

  @Override
  public List<EventOutbox> findForUpdateBy(OutboxStatus status, int limit, Instant processingStartedBefore) {
    return dsl.selectFrom(EVENT_OUTBOX.TABLE)
        .where(EVENT_OUTBOX.STATUS.eq(status)
            .and(EVENT_OUTBOX.PROCESSING_STARTED_AT.lessThan(processingStartedBefore)))
        .orderBy(EVENT_OUTBOX.CREATED_AT.asc())
        .limit(limit)
        .forUpdate()
        .skipLocked()
        .fetchInto(EventOutbox.class);
  }

  @Override
  public List<EventOutbox> claimForUpdateBy(OutboxStatus status, int limit) {
    return dsl.update(EVENT_OUTBOX.TABLE)
        .set(EVENT_OUTBOX.STATUS, PROCESSING)
        .set(EVENT_OUTBOX.PROCESSING_STARTED_AT, Instant.now())
        .where(EVENT_OUTBOX.STATUS.eq(status))
        .orderBy(EVENT_OUTBOX.CREATED_AT.asc())
        .limit(limit)
        .returning(EVENT_OUTBOX.COLUMNS)  // or specific fields
        .fetchInto(EventOutbox.class);
  }

  @Override
  public Optional<EventOutbox> claimForUpdateBy(EventOutbox event) {
    return dsl.update(EVENT_OUTBOX.TABLE)
        .set(EVENT_OUTBOX.STATUS, PROCESSING)
        .set(EVENT_OUTBOX.PROCESSING_STARTED_AT, Instant.now())
        .where(EVENT_OUTBOX.EVENT_ID.eq(event.id())
            .and(EVENT_OUTBOX.STATUS.eq(PENDING)))
        .returning(EVENT_OUTBOX.COLUMNS)
        .fetchOptionalInto(EventOutbox.class);
  }

  @Override
  public EventOutbox update(EventOutbox event) {
    return dsl.update(EVENT_OUTBOX.TABLE)
        .set(EVENT_OUTBOX.STATUS, event.status())
        .set(EVENT_OUTBOX.RETRIES, event.retries())
        .where(EVENT_OUTBOX.EVENT_ID.eq(event.id()))
        .returning(EVENT_OUTBOX.COLUMNS)
        .fetchOneInto(EventOutbox.class);
  }
}
