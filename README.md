# kafka-flink-schema-consolidation

Reference implementation for the discriminator-based schema consolidation pattern described in the InfoQ article [The Schema Proliferation Problem in Kafka and Flink Pipelines: How to Solve It](https://www.infoq.com/articles/schema-proliferation-problem/).

## The problem

One schema per event type is clean at first. It becomes painful once your event catalog grows and most schemas share 80-95% of their fields:

- A change to a shared field requires updating N schemas and N adapters
- Cross-variant queries require UNION across N tables
- Schema drift builds up when definitions are maintained separately
- Every new event variant needs a new schema, a new table, and adapter code from scratch

In this example: 4 event types x 3 ride types = **12 fragmented schemas and 12 downstream tables**.

## The solution

Collapse all variants into one consolidated schema. Use discriminator enum fields (`eventType`, `rideType`) to identify each variant. Use nullable attribute blocks for variant-specific data.

```sql
-- Before: required UNION across 12 tables
-- After: one table, one filter
SELECT * FROM driver_ride_activity
WHERE event_type = 'COMPLETED'
  AND ride_type = 'SHARED'
```

## Architecture

```
Kafka ("ride-events" topic, all variants in one stream)
    |
    v
RawRideEventDeserializer  turns the raw JSON bytes into a RawRideEvent
    |
    v
ConsolidationAdapter      reads eventType + rideType, delegates to the registry
    |
    v
AdapterRegistry           maps (EventType, RideType) to the right RecordAdapter
    |
    +-- StandardRideAcceptedAdapter
    +-- SharedRideAcceptedAdapter
    +-- ScheduledRideAcceptedAdapter
    +-- ... (12 adapters total, one per variant)
    |
    v
sink: prints each DriverRideActivityRecord to stdout in this demo
      (in production, one Iceberg table: driver_ride_activity)
```

## Two-layer design

The two layers keep transformation logic out of the Flink-specific code.

**Layer 1: RecordAdapter (pure Java, no Flink dependency)**

Each source event type has its own adapter. No Flink imports, so each one can be unit tested without any cluster setup.

```java
public class SharedRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedSharedEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord adapt(String orgId, DriverRideAcceptedSharedEvent event) {
        DriverRideActivityRecord record = new DriverRideActivityRecord();
        record.setEventTime(event.getTimestamp());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(EventType.ACCEPTED);
        record.setRideType(RideType.SHARED);

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        // scheduledRideAttributes remains null
        return record;
    }
}
```

**Layer 2: ConsolidationAdapter (Flink integration)**

`RawRideEventDeserializer` turns the Kafka bytes into a `RawRideEvent`. The adapter reads the discriminators and routes to the registry. All transformation logic lives in Layer 1.

```java
public class ConsolidationAdapter implements MapFunction<RawRideEvent, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord map(RawRideEvent event) {
        Map<String, Object> rawEvent = event.getFields();
        EventType eventType = resolveEventType(rawEvent);
        RideType rideType = resolveRideType(rawEvent);
        return adapterRegistry.adapt("default", eventType, rideType, rawEvent);
    }
}
```

## Repository structure

```
src/main/avro/
  fragmented/      # Before: 12 separate schemas, one per variant
  consolidated/    # After: one schema for all variants

src/main/java/com/example/consolidation/
  events/          # typed source events (plus a shared BaseRideEvent)
  adapter/         # the 12 adapters, the registry, the deserializer, and the output record
  model/           # the EventType and RideType discriminators
  job/             # RideActivityConsolidationJob, the Flink entry point

src/test/          # adapter and registry tests, no infrastructure needed
```

## Consolidated Avro schema

```json
{
  "type": "record",
  "name": "DriverRideActivityRecord",
  "fields": [
    {"name": "eventTime",   "type": "long"},
    {"name": "driverId",    "type": "string"},
    {"name": "rideId",      "type": "string"},
    {"name": "eventType",   "type": {"type": "enum", "name": "EventType",
                             "symbols": ["ACCEPTED", "STARTED", "COMPLETED", "CANCELLED"]}},
    {"name": "rideType",    "type": {"type": "enum", "name": "RideType",
                             "symbols": ["STANDARD", "SHARED", "SCHEDULED"]}},
    {"name": "standardRideAttributes",  "type": ["null", "StandardRideAttributes"],  "default": null},
    {"name": "sharedRideAttributes",    "type": ["null", "SharedRideAttributes"],    "default": null},
    {"name": "scheduledRideAttributes", "type": ["null", "ScheduledRideAttributes"], "default": null}
  ]
}
```

Discriminator enums are always populated. Nullable attribute blocks carry variant-specific data. Exactly one block is populated per record; the rest are null.

This snippet is abbreviated. The full schema in `src/main/avro/consolidated/DriverRideActivityRecord.avsc` also carries the base ride fields and the event-specific ones: `fareAmount`, `durationMins`, `distanceKm` (COMPLETED) and `cancellationReason` (CANCELLED).

## Adding a new variant

To add PREMIUM rides:

1. Add `PREMIUM` to `RideType.java`
2. Add a `PremiumRideAttributes` class, plus a `premiumRideAttributes` field and its getter/setter on `DriverRideActivityRecord.java`
3. Create the `DriverRide*PremiumEvent` source event classes in `events/`
4. Create the `PremiumRide*Adapter` classes implementing `RecordAdapter`
5. Register them in `AdapterRegistry.withAllAdapters()`, one line per event type
6. Add a matching `premiumRideAttributes` block to `DriverRideActivityRecord.avsc` (nullable, `"default": null`)

No existing adapters, consumers, or tables change.

## Schema evolution

New attribute blocks must be nullable with `"default": null`. Consumers compiled against the old schema read new records and see null for the new block. They don't break and don't need to be redeployed.

For Schema Registry: use `FULL` or `FULL_TRANSITIVE` compatibility mode. `BACKWARD` alone is not safe for new enum values, since a consumer compiled against the old schema may throw on an unknown symbol.

## Getting started

### Prerequisites

- Java 11+ and Maven 3.8+ (Maven downloads the dependencies on first build)
- Docker with Compose v2 (the `docker compose` command, included in Docker Desktop) to run Kafka locally; the `docker-compose.yml` here starts a single broker

The tests need only Java and Maven. The end-to-end run adds Docker. You do not need to install Flink or Kafka yourself: `mvn exec:exec` runs the job in an embedded local cluster, and Docker provides the broker. A real `flink run` submission is shown at the end, for when you have a cluster.

### Run the tests

```bash
mvn test
```

This is the quickest way to see the pattern work: the tests exercise all 12 adapters and the registry with no Kafka, Flink, or other infrastructure.

### Build

```bash
mvn clean package -DskipTests
```

Produces `target/kafka-flink-schema-consolidation-1.0.0.jar`.

### Run the job end to end

The job reads JSON events off the `ride-events` topic, routes each through the adapters, and prints the consolidated record. You produce events in one terminal and watch records appear in the other.

1. Start Kafka and create the topic:

   ```bash
   docker compose up -d
   docker exec kfsc-kafka /opt/kafka/bin/kafka-topics.sh \
     --create --topic ride-events --bootstrap-server localhost:9092
   ```

2. Run the job (embedded Flink, no install needed). It keeps running and prints each record, so leave it open:

   ```bash
   mvn compile exec:exec
   ```

   The exec config in `pom.xml` adds the module flags Flink needs on Java 17, so this works on both Java 11 and 17.

3. In another terminal, produce an event:

   ```bash
   echo '{"eventType":"ACCEPTED","rideType":"SHARED","eventTime":1700000000000,"driverId":"driver-1","rideId":"ride-1","cityId":"NYC","pickupLat":40.71,"pickupLng":-74.0,"estimatedDurationMinutes":12,"estimatedFare":18.5,"passengerCount":2,"poolingScore":0.87}' \
     | docker exec -i kfsc-kafka /opt/kafka/bin/kafka-console-producer.sh \
       --topic ride-events --bootstrap-server localhost:9092
   ```

The job terminal prints the consolidated record (only the populated blocks show):

```
Consolidating 'ride-events' -> s3://your-bucket/driver_ride_activity
DriverRideActivityRecord{eventType=ACCEPTED, rideType=SHARED, driverId=driver-1, rideId=ride-1, cityId=NYC, sharedRideAttributes={passengerCount=2, poolingScore=0.87}}
```

Change `eventType`, `rideType`, and the variant fields to exercise the other adapters. A `COMPLETED` event adds `fareAmount`, `durationMins`, and `distanceKm`; a `CANCELLED` event adds `cancellationReason`. Stop the job with Ctrl+C and tear down Kafka with `docker compose down`.

#### On a real Flink cluster

If you already have a cluster, build the fat jar (see Build above) and submit it. The cluster has Flink on its classpath, so `flink run` adds the Java 17 module flags for you:

```bash
flink run -c com.example.consolidation.job.RideActivityConsolidationJob \
  target/kafka-flink-schema-consolidation-1.0.0.jar \
  --kafka-bootstrap-servers <broker:9092> \
  --output-path s3://your-bucket/driver_ride_activity \
  --checkpoint-interval 60000
```

To stand up a real cluster locally instead, `docker-compose.cluster.yml` runs a JobManager, a TaskManager, and Kafka. This is closer to production than `mvn exec:exec` (the job is submitted to a cluster and runs on the TaskManager), with the Flink dashboard at http://localhost:8081.

```bash
# 1. build the jar (the compose mounts ./target into the JobManager)
mvn clean package -DskipTests
docker compose -f docker-compose.cluster.yml up -d

# 2. create the topic
docker compose -f docker-compose.cluster.yml exec kafka \
  /opt/kafka/bin/kafka-topics.sh --create --topic ride-events --bootstrap-server kafka:9092

# 3. submit the jar (mounted at /jars)
docker compose -f docker-compose.cluster.yml exec jobmanager \
  flink run -d -c com.example.consolidation.job.RideActivityConsolidationJob \
  /jars/kafka-flink-schema-consolidation-1.0.0.jar --kafka-bootstrap-servers kafka:9092

# 4. produce an event
echo '{"eventType":"ACCEPTED","rideType":"SHARED","eventTime":1700000000000,"driverId":"driver-1","rideId":"ride-1","cityId":"NYC","pickupLat":40.71,"pickupLng":-74.0,"estimatedDurationMinutes":12,"estimatedFare":18.5,"passengerCount":2,"poolingScore":0.87}' \
  | docker compose -f docker-compose.cluster.yml exec -T kafka \
    /opt/kafka/bin/kafka-console-producer.sh --topic ride-events --bootstrap-server kafka:9092
```

The job runs on the TaskManager, so its output goes to that container's log:

```bash
docker compose -f docker-compose.cluster.yml logs taskmanager | grep DriverRideActivityRecord
# DriverRideActivityRecord{eventType=ACCEPTED, rideType=SHARED, driverId=driver-1, ...}
```

Tear it all down with `docker compose -f docker-compose.cluster.yml down -v`.

## Trade-offs

**Wider records.** Nullable attribute blocks are empty for most records. Avro handles nulls cheaply, but at very high throughput it is worth benchmarking the serialization overhead.

**Schema governance.** One schema shared across teams needs clear ownership. A Schema Registry with enforced compatibility rules handles the mechanical side, but someone still needs to decide what goes in and what stays out.

**Debugging.** You need a `WHERE eventType = '...'` filter that you didn't need when each event type had its own table. Not expensive, just a habit to build.

**When not to use it.** This pattern makes sense when event types share structural overlap and are queried together. If two event types are structurally unrelated and never queried in the same context, consolidating them adds complexity for no benefit.

## License

Apache 2.0
