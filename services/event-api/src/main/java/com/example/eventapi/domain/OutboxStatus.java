package com.example.eventapi.domain;

public enum OutboxStatus {
  PENDING,
  PROCESSING,
  SENT,
  FAILED;
}
