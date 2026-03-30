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
ConsolidationAdapter     parses raw event, reads eventType + rideType, delegates to registry
    |
    v
AdapterRegistry          maps (EventType, RideType) to the right RecordAdapter
    |
    +-- StandardRideAcceptedAdapter
    +-- SharedRideAcceptedAdapter
    +-- ScheduledRideAcceptedAdapter
    +-- ... (12 adapters total, one per variant)
    |
    v
S3 / Data Lake
    +-- driver_ride_activity  (single consolidated Iceberg table)
```

## Two-layer design

Transformation logic is kept separate from Flink framework integration.

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
        record.setEventType(EventType.ACCEPTED);
        record.setRideType(RideType.SHARED);
        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        return record;
    }
}
```

**Layer 2: ConsolidationAdapter (Flink integration)**

Routes events to the right adapter via the registry. All transformation logic lives in Layer 1.

```java
public class ConsolidationAdapter implements MapFunction<String, DriverRideActivityRecord> {

    @Override
    public DriverRideActivityRecord map(String rawEventJson) throws Exception {
        Map<String, Object> rawEvent = parseEvent(rawEventJson);
        EventType eventType = resolveEventType(rawEvent);
        RideType rideType = resolveRideType(rawEvent);
        return adapterRegistry.adapt("default", eventType, rideType, rawEvent);
    }
}
```

## Repository structure

```
src/
  main/
    avro/
      fragmented/                         # Before: separate schemas per variant
        DriverRideAcceptedStandard.avsc
        DriverRideAcceptedShared.avsc
        DriverRideAcceptedScheduled.avsc
      consolidated/                       # After: one schema for all variants
        DriverRideActivityRecord.avsc
    java/com/example/consolidation/
      events/                             # 12 typed source event classes
        BaseRideEvent.java
        DriverRideAcceptedStandardEvent.java
        DriverRideAcceptedSharedEvent.java
        DriverRideAcceptedScheduledEvent.java
        ... (and 9 more for STARTED, COMPLETED, CANCELLED x 3 ride types)
      adapter/
        RecordAdapter.java                # Interface: adapt(orgId, event) -> DriverRideActivityRecord
        AdapterRegistry.java             # Maps (EventType, RideType) to the right adapter
        ConsolidationAdapter.java        # Flink MapFunction, delegates to registry
        DriverRideActivityRecord.java    # Output POJO matching the Avro schema
        StandardRideAttributes.java
        SharedRideAttributes.java
        ScheduledRideAttributes.java
        StandardRideAcceptedAdapter.java
        SharedRideAcceptedAdapter.java
        ... (and 9 more adapters)
      model/
        EventType.java                   # Enum: ACCEPTED, STARTED, COMPLETED, CANCELLED
        RideType.java                    # Enum: STANDARD, SHARED, SCHEDULED
      job/
        RideActivityConsolidationJob.java
  test/
    java/com/example/consolidation/adapter/
      SharedRideAcceptedAdapterTest.java  # Tests the adapter in isolation, no Flink needed
      AdapterRegistryTest.java           # Verifies all 12 combinations route correctly
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

## Adding a new variant

To add PREMIUM rides:

1. Add `PREMIUM` to `RideType.java`
2. Create `DriverRide*PremiumEvent` source event classes in `events/`
3. Create `PremiumRide*Adapter` classes implementing `RecordAdapter`
4. Register them in `AdapterRegistry.withAllAdapters()`, one call per event type
5. Add a `premiumRideAttributes` nullable block to `DriverRideActivityRecord.avsc` with `"default": null`

No existing adapters, consumers, or tables need to change.

## Schema evolution

New attribute blocks must be nullable with `"default": null`. Consumers compiled against the old schema read new records and see null for the new block. They don't break and don't need to be redeployed.

For Schema Registry: use `FULL` or `FULL_TRANSITIVE` compatibility mode. `BACKWARD` alone is not safe for new enum values, since a consumer compiled against the old schema may throw on an unknown symbol.

## Getting started

### Prerequisites

- Java 11+
- Maven 3.8+
- Apache Flink 1.18+
- Apache Kafka 3.x (only needed to run the job, not for tests)

### Run the tests

```bash
mvn test
```

Tests run without Kafka, Flink, or any infrastructure.

### Build

```bash
mvn clean package -DskipTests
```

### Run the Flink job

```bash
flink run -c com.example.consolidation.job.RideActivityConsolidationJob \
  target/kafka-flink-schema-consolidation-1.0.0.jar \
  --kafka-bootstrap-servers localhost:9092 \
  --output-path s3://your-bucket/driver_ride_activity \
  --checkpoint-interval 60000
```

## Trade-offs

**Wider records.** Nullable attribute blocks are empty for most records. Avro handles nulls cheaply, but at very high throughput it is worth benchmarking the serialization overhead.

**Schema governance.** One schema shared across teams needs clear ownership. A Schema Registry with enforced compatibility rules handles the mechanical side, but someone still needs to decide what goes in and what stays out.

**Debugging.** You need a `WHERE eventType = '...'` filter that you didn't need when each event type had its own table. Not expensive, just a habit to build.

**When not to use it.** This pattern makes sense when event types share structural overlap and are queried together. If two event types are structurally unrelated and never queried in the same context, consolidating them adds complexity for no benefit.

## License

Apache 2.0
