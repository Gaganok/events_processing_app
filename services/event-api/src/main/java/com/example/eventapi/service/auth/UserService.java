package com.example.eventapi.service.auth;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public record UserService(PasswordEncoder encoder) implements UserDetailsService {

  @Override
  public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
    return switch (email.toLowerCase()) {
      case "user@example.com" -> User.withUsername("user@example.com")
          .password(encoder.encode("password"))
          .roles("USER")
          .build();

      case "admin@example.com" -> User.withUsername("admin@example.com")
          .password(encoder.encode("admin123"))
          .roles("ADMIN")
          .build();

      default -> throw new UsernameNotFoundException("User not found");
    };
  }
}