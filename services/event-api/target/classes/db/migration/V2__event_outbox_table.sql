CREATE TABLE events_outbox (
    id UUID PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    retries INT NOT NULL,
    processing_started_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_outbox_status_created_at ON events_outbox (status, created_at) WHERE status = 'PENDING';