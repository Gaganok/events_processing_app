package com.example.eventapi.repository;

import com.example.eventapi.domain.Event;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.example.eventapi.repository.Schema.EVENT;

public record PgEventRepository(DSLContext dsl) implements EventRepository {

  @Override
  public void insert(Event event) {
    dsl.insertInto(EVENT.TABLE)
        .set(EVENT.ID, event.id())
        .set(EVENT.PAYLOAD, event.payload())
        .set(EVENT.STATUS, event.status())
        .set(EVENT.CREATED_AT, event.createdAt())
        .execute();
  }

  @Override
  public void delete(UUID eventId) {
    dsl.deleteFrom(EVENT.TABLE)
        .where(EVENT.ID.eq(eventId))
        .execute();
  }

  @Override
  public List<Event> findByIds(Set<UUID> eventIds) {
    return dsl.selectFrom(EVENT.TABLE)
        .where(EVENT.ID.in(eventIds))
        .fetchInto(Event.class);
  }
}
