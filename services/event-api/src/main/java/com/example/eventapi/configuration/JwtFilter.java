package com.example.eventapi.configuration;

import com.example.eventapi.utils.JwtResolver;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static java.util.Optional.ofNullable;

public class JwtFilter extends OncePerRequestFilter {

  private final UserDetailsService userService;
  public static final String AUTHORIZATION_HEADER = "Authorization";
  private final JwtResolver jwt;

  public JwtFilter(UserDetailsService userService, JwtResolver jwt) {
    this.userService = userService;
    this.jwt = jwt;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req,
      HttpServletResponse res,
      FilterChain chain
  ) throws ServletException, IOException {

    var maybeHeader = ofNullable(req.getHeader(AUTHORIZATION_HEADER))
        .filter(header -> header.startsWith("Bearer "))
        .map(header -> header.substring(7));

    if (maybeHeader.isEmpty()) {
      chain.doFilter(req, res);
      return;
    }

    try {
      String token = maybeHeader.get();
      String username = jwt.extractUsername(token);

      if (username != null
          && SecurityContextHolder.getContext().getAuthentication() == null) {

        authenticateUser(token, username);
      }

    } catch (JwtException | IllegalArgumentException ex) {
      SecurityContextHolder.clearContext();
    }

    chain.doFilter(req, res);
  }

  private void authenticateUser(String token, String username) {
    if (!jwt.isValid(token)) return;
    var user = userService.loadUserByUsername(username);
    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}