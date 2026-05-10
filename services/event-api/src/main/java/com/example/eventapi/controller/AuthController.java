package com.example.eventapi.controller;

import com.example.eventapi.controller.dto.LoginRequest;
import com.example.eventapi.controller.dto.LoginResponse;
import com.example.eventapi.utils.JwtResolver;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public record AuthController(AuthenticationManager authenticationManager, JwtResolver jwt) {

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    var authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.username(), request.password())
    );

    var userDetails = (UserDetails) authentication.getPrincipal();
    var jwtToken = jwt.generateToken(userDetails.getUsername(), userDetails.getAuthorities());
    return LoginResponse.from(jwtToken);
  }
}
