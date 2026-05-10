package com.example.eventapi.controller;

import com.example.eventapi.service.EventService;
import com.example.eventapi.utils.JwtResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static java.time.OffsetDateTime.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerSecurityTest {

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private EventService eventService;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    doNothing().when(eventService).delete(any(UUID.class));
    doNothing().when(eventService).create(any());
  }

  private final String validEventJson = """
      { "name": "Tech Conference", "date": "2026-12-01" }
      """;

  @Test
  void createWithUserRoleReturnsOk() throws Exception {
    mockMvc.perform(post("/events")
            .header("Authorization", "Bearer " + userToken())
            .contentType(APPLICATION_JSON)
            .content(validEventJson))
        .andExpect(status().isOk());
  }

  @Test
  void deleteWithAdminRoleReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/events/" + UUID.randomUUID())
            .header("Authorization", "Bearer " + adminToken()))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteWithUserRoleReturnsForbidden() throws Exception {
    mockMvc.perform(delete("/events/" + UUID.randomUUID())
            .header("Authorization", "Bearer " + userToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void createWithoutTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/events")
            .contentType(APPLICATION_JSON)
            .content(validEventJson))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createWithInvalidTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/events")
            .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalid")
            .contentType(APPLICATION_JSON)
            .content(validEventJson))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createWithExpiredTokenReturnsUnauthorized() throws Exception {
    mockMvc.perform(post("/events")
            .header("Authorization", "Bearer " + expiredToken())
            .contentType(APPLICATION_JSON)
            .content(validEventJson))
        .andExpect(status().isUnauthorized());
  }

  private String expiredToken() {
    return jwt().generateToken("user@example.com", List.of(), now().minusDays(5)).token();
  }

  private String userToken() {
    return jwt().generateToken("user@example.com", List.of(new SimpleGrantedAuthority("ROLE_USER")), now()).token();
  }

  private String adminToken() {
    return jwt().generateToken("admin@example.com", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), now()).token();
  }

  private JwtResolver jwt() {
    return new JwtResolver(
        "Zm9yLXNlY3JldC1rZXktc2hvdWxkLWJlLTI1Ni1iaXRzLXJlcXVpcmVkLWFuZC1iZS1hdC1sZWFzdC0yNTYtYml0cw==",
        60 * 5
    );
  }
}