package com.example.eventapi.controller.dto;

import static com.example.eventapi.controller.dto.EventResponseStatus.ACCEPTED;

public record EventResponse(String id, EventResponseStatus status) {
  public static EventResponse accepted(String id) {
    return new EventResponse(id, ACCEPTED);
  }
}

