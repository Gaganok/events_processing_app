package com.example.eventapi.repository;

import com.example.eventapi.domain.Event;
import org.jooq.DSLContext;

import static com.example.eventapi.repository.Schema.EVENT;

public record PgEventRepository(DSLContext dsl) implements EventRepository {

  @Override
  public void insert(Event event) {
    dsl.insertInto(EVENT.TABLE)
        .set(EVENT.ID, event.id())
        .set(EVENT.PAYLOAD, event.payload())
        .set(EVENT.STATUS, event.status())
        .execute();
  }
}
