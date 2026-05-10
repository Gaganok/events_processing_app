package com.example.eventapi.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;

import static java.time.OffsetDateTime.now;

public record JwtResolver(String secret, long expiresIn) {

  public JwtToken generateToken(String email, Collection<? extends GrantedAuthority> authorities, OffsetDateTime currentTime) {
    var expiresAt = currentTime.plusMinutes(expiresIn);
    var token = Jwts.builder()
        .subject(email)
        .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).toList())
        .issuedAt(new Date())
        .expiration(new Date(expiresAt.toInstant().toEpochMilli()))
        .signWith(key())
        .compact();

    return new JwtToken(token, expiresAt);
  }

  public JwtToken generateToken(String email, Collection<? extends GrantedAuthority> authorities) {
    return generateToken(email, authorities, now());
  }

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(key())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public boolean isValid(String token) {
    try {
      return !Jwts.parser().verifyWith(key()).build()
          .parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private SecretKey key() {
    byte[] bytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(bytes);
  }

  public record JwtToken(String token, OffsetDateTime expiresAt) {
  }
}