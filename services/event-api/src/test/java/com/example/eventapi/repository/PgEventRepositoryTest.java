package com.example.eventapi.repository;


import com.example.eventapi.configuration.ContainerConfiguration;
import com.example.eventapi.domain.Event;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static com.example.eventapi.repository.Schema.EVENT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(ContainerConfiguration.class)
class PgEventRepositoryTest {

  @Autowired
  private DSLContext dsl;
  private EventRepository underTest;

  @BeforeEach
  void setUp() {
    this.underTest = new PgEventRepository(dsl);
  }

  @Test
  void shouldInsertEvent() {
    var event = Event.from("{\"type\":\"USER_CREATED\"}");
    var expected = event.id();

    underTest.insert(event);

    var result = dsl.selectFrom(EVENT.TABLE)
        .where(EVENT.ID.eq(expected))
        .fetchOne();

    assertThat(result).isNotNull();
    assertThat(result.get(EVENT.ID)).isEqualTo(expected);
  }
}