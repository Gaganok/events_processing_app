package com.example.eventapi.domain.auth;

import java.util.UUID;

public record User(UUID id, String username, String password) {
}
