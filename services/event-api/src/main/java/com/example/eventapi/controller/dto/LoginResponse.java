package com.example.eventapi.controller.dto;

import com.example.eventapi.utils.JwtResolver;

import java.time.OffsetDateTime;

public record LoginResponse(String token, OffsetDateTime expiresAt) {
  public static LoginResponse from(JwtResolver.JwtToken jwtToken) {
    return new LoginResponse(jwtToken.token(), jwtToken.expiresAt());
  }
}
