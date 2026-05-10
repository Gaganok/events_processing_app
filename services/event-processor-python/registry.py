import threading

from dataclasses import dataclass
from datetime import datetime, timezone
from enum import StrEnum

class EventStatus(StrEnum):
    PENDING = "pending"
    PROCESSED = "processed"

@dataclass
class EventRecord:
    status: EventStatus
    object_key: str | None = None
    processed_at: datetime | None = None


class EventRegistry:
    def __init__(self):
        self._store: dict[str, EventRecord] = {}
        self._lock = threading.Lock()

    def mark_pending(self, event_id: str) -> None:
        with self._lock:
            self._store[event_id] = EventRecord(
                status=EventStatus.PENDING
            )

    def mark_processed(self, event_id: str, object_key: str) -> None:
        with self._lock:
            self._store[event_id] = EventRecord(
                status=EventStatus.PROCESSED,
                object_key=object_key,
                processed_at=datetime.now(timezone.utc),
            )

    def get(self, event_id: str) -> EventRecord | None:
        with self._lock:
            return self._store.get(event_id)