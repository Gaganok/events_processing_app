import json
import uuid
from io import BytesIO

import xmltodict
from confluent_kafka import Consumer, KafkaException
from minio import Minio

from registry import EventRegistry
from health import make_app, start_http_server

import logging

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s %(message)s",
)

logger = logging.getLogger(__name__)

KAFKA_BOOTSTRAP = "localhost:9092"
MINIO_ENDPOINT = "localhost:9000"
MINIO_ACCESS_KEY = "minioadmin"
MINIO_SECRET_KEY = "minioadmin"

TOPIC = "events"
BUCKET = "events"
GROUP_ID = "event-processor-group"

def ensure_bucket(client: Minio) -> None:
    if not client.bucket_exists(BUCKET):
        client.make_bucket(BUCKET)
        print(f'Created bucket "{BUCKET}"', flush=True)


def make_minio_client() -> Minio:
    return Minio(
        MINIO_ENDPOINT,
        access_key=MINIO_ACCESS_KEY,
        secret_key=MINIO_SECRET_KEY,
        secure=False,
    )


def make_consumer() -> Consumer:
    return Consumer(
        {
            "bootstrap.servers": KAFKA_BOOTSTRAP,
            "group.id": GROUP_ID,
            "auto.offset.reset": "earliest",
            "enable.auto.commit": False,
        }
    )


def run_consumer_loop(consumer: Consumer, minio_client: Minio, registry: EventRegistry) -> None:
    consumer.subscribe([TOPIC])
    logger.info("Consumer started, waiting for messages")

    while True:
        msg = consumer.poll(1.0)
        if msg is None:
            continue

        if msg.error():
            logger.error("Kafka error: %s", msg.error())
            continue

        try:
            payload = json.loads(msg.value().decode("utf-8"))
        except Exception as e:
            logger.warning("Skip invalid JSON: %s", e)
            continue

        event_id = str(payload.get("id") or uuid.uuid4())

        registry.mark_pending(event_id)

        try:
            xml_bytes = xmltodict.unparse({"event": payload}, pretty=True).encode("utf-8")
            object_key = f"{event_id}.xml"

            minio_client.put_object(
                BUCKET,
                object_key,
                BytesIO(xml_bytes),
                length=len(xml_bytes),
                content_type="application/xml",
            )

            registry.mark_processed(event_id, object_key)
            consumer.commit(msg)
            print(f"Processed event_id={event_id} → {object_key}", flush=True)

        except Exception as e:
            logger.exception("Failed processing event_id=%s", event_id)


def main() -> None:
    registry = EventRegistry()
    minio_client = make_minio_client()
    ensure_bucket(minio_client)

    app = make_app(registry, {"bootstrap.servers": KAFKA_BOOTSTRAP}, minio_client)
    start_http_server(app)

    consumer = make_consumer()
    try:
        run_consumer_loop(consumer, minio_client, registry)
    finally:
        consumer.close()


if __name__ == "__main__":
    main()
