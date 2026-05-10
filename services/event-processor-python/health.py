import threading

import fastapi
import uvicorn
import logging

from registry import EventRegistry
from confluent_kafka.admin import AdminClient

logger = logging.getLogger(__name__)

def make_app(registry: EventRegistry, kafka_cfg: dict, minio_client) -> fastapi.FastAPI:
    app = fastapi.FastAPI()

    kafka_admin = AdminClient({
        "bootstrap.servers": kafka_cfg["bootstrap.servers"]
    })

    @app.get("/health")
    def health():
        errors = []

        try:
            kafka_admin.list_topics(timeout=3)
        except Exception as e:
            logger.exception("Kafka health check failed")
            errors.append(f"kafka: {e}")

        try:
            minio_client.bucket_exists("healthcheck")
        except Exception as e:
            logger.exception("MinIO health check failed")
            errors.append(f"minio: {e}")

        if errors:
            return fastapi.responses.JSONResponse(
                {"status": "unhealthy", "errors": errors}, status_code=503
            )
        return {"status": "ok"}

    @app.get("/events/{event_id}/status")
    def event_status(event_id: str):
        record = registry.get(event_id)
        if record is None:
            raise fastapi.HTTPException(status_code=404, detail="Event not found")
        if record.status == "processed":
            return {
                "status": "processed",
                "objectKey": record.object_key,
                "processedAt": record.processed_at.isoformat(),
            }
        return {"status": "pending"}

    return app


def start_http_server(app, host="0.0.0.0", port=8081):
    config = uvicorn.Config(app, host=host, port=port)
    server = uvicorn.Server(config)

    thread = threading.Thread(target=server.run, daemon=True)
    thread.start()

    return server, thread