package com.example.eventapi.controller;

import com.example.eventapi.controller.dto.EventResponse;
import com.example.eventapi.domain.Event;
import com.example.eventapi.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static com.example.eventapi.controller.dto.EventResponse.accepted;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/events")
public class EventController {

  private final EventService eventService;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<EventResponse> create(@RequestBody String raw) {
    var cleanJson = validateAndCleanJson(raw);
    var event = Event.from(cleanJson);
    eventService.create(event);
    return ok(accepted(event.id().toString()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    eventService.delete(UUID.fromString(id));
    return noContent().build();
  }

  private String validateAndCleanJson(String json) {
    objectMapper.readTree(json);
    return json.replaceAll("\\s+", "");
  }
}
