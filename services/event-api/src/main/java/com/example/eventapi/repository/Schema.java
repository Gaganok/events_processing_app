package com.example.eventapi.repository;

import com.example.eventapi.domain.EventStatus;
import com.example.eventapi.domain.OutboxStatus;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.EnumConverter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.VARCHAR;

public final class Schema {

  private Schema() {
  }

  public static final EventTable EVENT = new EventTable();
  public static final EventOutboxTable EVENT_OUTBOX = new EventOutboxTable();

  public static final class EventOutboxTable {
    public final Table<Record> TABLE = tableName("events_outbox");
    public final Field<UUID> EVENT_ID = tableField(TABLE, "id", UUID.class);
    public final Field<OutboxStatus> STATUS = tableField(TABLE, "status", enumType(OutboxStatus.class));
    public final Field<Integer> RETRIES = tableField(TABLE, "retries", Integer.class);
    public final Field<Instant> PROCESSING_STARTED_AT = tableField(TABLE, "processing_started_at", Instant.class);
    public final Field<Instant> CREATED_AT = tableField(TABLE, "created_at", Instant.class);
    public final List<Field<?>> COLUMNS = List.of(EVENT_ID, STATUS, RETRIES, CREATED_AT);

    private EventOutboxTable() {
    }
  }

  public static final class EventTable {
    public final Table<Record> TABLE = tableName("events");
    public final Field<UUID> ID = tableField(TABLE, "id", UUID.class);
    public final Field<String> PAYLOAD = tableField(TABLE, "payload", String.class);
    public final Field<EventStatus> STATUS = tableField(TABLE, "status", enumType(EventStatus.class));
    public final Field<Instant> CREATED_AT = tableField(TABLE, "created_at", Instant.class);
    public final List<Field<?>> COLUMNS = List.of(ID, PAYLOAD, STATUS, CREATED_AT);

    private EventTable() {
    }
  }

  private static Table<Record> tableName(String name) {
    return table(name(name));
  }

  private static <T> Field<T> tableField(Table<?> table, String name, Class<T> type) {
    return field(name(table.getQualifiedName(), name(name)), type);
  }

  private static <T> Field<T> tableField(Table<?> table, String name, DataType<T> type) {
    return field(name(table.getQualifiedName(), name(name)), type);
  }

  private static <E extends Enum<E>> DataType<E> enumType(Class<E> enumClass) {
    return VARCHAR.asConvertedDataType(new EnumConverter<>(String.class, enumClass));
  }
}
