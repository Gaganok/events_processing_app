package com.example.eventapi.domain;

public record EventMessage(String id, String payload) {
  public static EventMessage from(Event event) {
    return new EventMessage(event.id().toString(), event.payload());
  }
}
