package com.example.eventapi.controller;

import com.example.eventapi.controller.dto.EventResponse;
import com.example.eventapi.domain.Event;
import com.example.eventapi.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import static com.example.eventapi.controller.dto.EventResponse.accepted;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/events")
public record EventController(EventService eventService) {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @PostMapping
  public ResponseEntity<EventResponse> create(@RequestBody String raw) {
    var cleanJson = validateAndCleanJson(raw);
    var event = Event.from(cleanJson);
    eventService.create(event);
    return ok(accepted(event.id().toString()));
  }

  private String validateAndCleanJson(String json) {
    objectMapper.readTree(json);
    return json.replaceAll("\\s+", "");
  }
}
