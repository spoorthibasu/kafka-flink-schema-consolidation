# Kafka-Flink Schema Consolidation

A reference implementation of the discriminator-based schema consolidation pattern for Apache Kafka and Apache Flink pipelines.

Demonstrates how to replace N fragmented event schemas with a single consolidated schema using a layered adapter design — separating pure transformation logic from Flink framework integration so each adapter can be unit tested without any cluster setup.

Companion to the InfoQ article: *The Schema Proliferation Problem in Kafka and Flink Pipelines: How to Solve It*

---

## The Problem

When each Kafka event type maps to its own schema and downstream table, the system works — until it doesn't:

- A shared field change requires updating N schemas and N adapters
- Downstream consumers union N tables to answer a single question
- Schema drift accumulates across independently maintained definitions
- Adding a new event variant means a new schema, a new table, and new adapter code from scratch

In this ride-sharing domain example, 4 event types × 3 ride types = **12 fragmented schemas and 12 downstream tables**.

---

## The Solution

Collapse all variants into one consolidated schema. Use discriminator enum fields (`eventType`, `rideType`) to identify the variant. Use nullable attribute blocks to carry variant-specific data.

**Result:** 12 schemas → 1. 12 tables → 1. Cross-variant queries need no UNION.

```sql
-- Before: required joining 12 tables
-- After: one table, one filter
SELECT * FROM driver_ride_activity
WHERE event_type = 'RIDE_COMPLETED'
  AND ride_type = 'SHARED'
```

---

## Architecture

```
Kafka Topics (4 topics × 3 ride types = 12 fragmented event variants)
    │
    ▼
Flink Job
    │
    ├── EventTypeRouter          (enriches raw event with _eventType, _rideType metadata)
    │
    ├── ConsolidationAdapter     (Flink MapFunction — reads discriminators, delegates to registry)
    │         │
    │         ▼
    │   AdapterRegistry          (maps (RideEventType, RideType) → RecordAdapter)
    │         │
    │         ├── StandardRideAcceptedAdapter
    │         ├── SharedRideAcceptedAdapter
    │         ├── ScheduledRideAcceptedAdapter
    │         ├── ... (12 adapters total, one per variant)
    │
    ▼
S3 / Data Lake
    └── driver_ride_activity     (single consolidated table)
```

---

## Two-Layer Design

The key architectural decision is separating transformation logic from framework integration.

**Layer 1 — RecordAdapter (pure Java, no Flink dependency)**

Each source event type has a dedicated adapter implementing `RecordAdapter<S, T>`:

```java
public class SharedRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedSharedEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideAcceptedSharedEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setEventType(RideEventType.RIDE_ACCEPTED);
        record.setRideType(RideType.SHARED);

        SharedRideAttributes attrs = new SharedRideAttributes();
        attrs.setPassengerCount(event.getPassengerCount());
        attrs.setPoolingScore(event.getPoolingScore());
        record.setSharedRideAttributes(attrs);
        return record;
    }
}
```

No Flink import. Straightforward to unit test without any framework setup.

**Layer 2 — ConsolidationAdapter (Flink integration)**

```java
public class ConsolidationAdapter implements MapFunction<Map<String, Object>, ConsolidatedRecord> {

    private final AdapterRegistry adapterRegistry;

    public ConsolidationAdapter(AdapterRegistry adapterRegistry) {
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public ConsolidatedRecord map(Map<String, Object> rawEvent) throws Exception {
        RideEventType eventType = resolveEventType(rawEvent);
        RideType rideType = resolveRideType(rawEvent);
        return adapterRegistry.adapt("default", eventType, rideType, rawEvent);
    }
}
```

All transformation logic lives in Layer 1. This class only wires Flink's `MapFunction` contract to the registry.

---

## Repository Structure

```
kafka-flink-schema-consolidation/
├── src/
│   ├── main/
│   │   ├── avro/
│   │   │   ├── fragmented/                         # Before: 3 separate schemas (showing the problem)
│   │   │   │   ├── DriverRideAcceptedStandard.avsc
│   │   │   │   ├── DriverRideAcceptedShared.avsc
│   │   │   │   └── DriverRideAcceptedScheduled.avsc
│   │   │   └── consolidated/                       # After: 1 unified schema
│   │   │       └── DriverRideActivityRecord.avsc
│   │   └── java/com/example/consolidation/
│   │       ├── events/                             # 12 typed source event classes
│   │       │   ├── BaseRideEvent.java
│   │       │   ├── DriverRideAcceptedStandardEvent.java
│   │       │   ├── DriverRideAcceptedSharedEvent.java
│   │       │   ├── DriverRideAcceptedScheduledEvent.java
│   │       │   └── ... (9 more for STARTED, COMPLETED, CANCELLED × 3 ride types)
│   │       ├── adapter/
│   │       │   ├── RecordAdapter.java              # Interface: adapt(orgId, event) → ConsolidatedRecord
│   │       │   ├── AdapterRegistry.java            # Maps (RideEventType, RideType) → adapter
│   │       │   ├── ConsolidationAdapter.java       # Flink MapFunction — delegates to registry
│   │       │   ├── ConsolidatedRecord.java         # Output POJO (mirrors Avro schema)
│   │       │   ├── SharedRideAttributes.java
│   │       │   ├── ScheduledRideAttributes.java
│   │       │   ├── StandardRideAcceptedAdapter.java
│   │       │   ├── SharedRideAcceptedAdapter.java
│   │       │   └── ... (9 more adapters)
│   │       ├── model/
│   │       │   ├── RideEventType.java              # Enum: RIDE_ACCEPTED, RIDE_STARTED, RIDE_COMPLETED, RIDE_CANCELLED
│   │       │   └── RideType.java                   # Enum: STANDARD, SHARED, SCHEDULED
│   │       └── job/
│   │           ├── EventTypeRouter.java
│   │           └── RideEventConsolidationJob.java
│   └── test/
│       └── java/com/example/consolidation/adapter/
│           ├── SharedRideAcceptedAdapterTest.java  # Tests adapter in isolation — no Flink needed
│           └── AdapterRegistryTest.java            # Verifies all 12 combinations route correctly
├── docs/
│   └── schema-design.md
├── pom.xml
└── README.md
```

---

## Consolidated Avro Schema

```json
{
  "type": "record",
  "name": "DriverRideActivityRecord",
  "fields": [
    {"name": "eventTime",   "type": "long"},
    {"name": "driverId",    "type": "string"},
    {"name": "rideId",      "type": "string"},
    {"name": "eventType",   "type": {"type": "enum", "name": "EventType",
                             "symbols": ["RIDE_ACCEPTED","RIDE_STARTED","RIDE_COMPLETED","RIDE_CANCELLED"]}},
    {"name": "rideType",    "type": {"type": "enum", "name": "RideType",
                             "symbols": ["STANDARD","SHARED","SCHEDULED"]}},
    {"name": "sharedRideAttributes",    "type": ["null", "SharedRideAttributes"],    "default": null},
    {"name": "scheduledRideAttributes", "type": ["null", "ScheduledRideAttributes"], "default": null}
  ]
}
```

Discriminator enums are always populated. Nullable attribute blocks carry variant-specific data — exactly one is populated per record, the rest are null.

---

## Adding a New Ride Variant

To add a new variant (e.g. PREMIUM rides):

1. Add `PREMIUM` to the `RideType` enum
2. Create `DriverRide*PremiumEvent` classes in `events/` with the new fields
3. Create `PremiumRide*Adapter` classes implementing `RecordAdapter`
4. Add one `registry.register(...)` call per event type in `AdapterRegistry.withAllAdapters()`
5. Add a `premiumRideAttributes` nullable block to the Avro schema

No existing adapters, consumers, or tables are touched.

---

## Schema Evolution

New attribute blocks must be nullable with `"default": null`. Existing consumers compiled against the old schema read new records and see null for the new block — they do not break and do not need to be redeployed.

For Schema Registry compatibility, use `FULL` or `FULL_TRANSITIVE` mode. `BACKWARD` mode alone is not safe when adding new enum values, as a consumer compiled against the old schema may throw on an unknown symbol.

---

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.8+
- Apache Flink 1.18+
- Apache Kafka 3.x (only needed to run the job; not needed for tests)

### Run the unit tests

```bash
mvn test
```

Tests run in isolation — no Kafka, no Flink cluster, no infrastructure required.

### Build

```bash
mvn clean package -DskipTests
```

### Run the Flink job

```bash
flink run -c com.example.consolidation.job.RideEventConsolidationJob \
  target/kafka-flink-schema-consolidation-1.0.0.jar \
  --kafka-bootstrap-servers localhost:9092 \
  --output-path s3://your-bucket/driver_ride_activity \
  --checkpoint-interval 60000
```

---

## Trade-offs

**Wider records.** Nullable attribute blocks are empty for most records. Avro's null handling keeps serialization cost minimal, but at very high throughput it is worth benchmarking.

**Schema governance.** A consolidated schema owned by multiple teams needs clear ownership. A Schema Registry with enforced compatibility rules handles the mechanical side, but someone still needs to own what goes into the schema.

**Debugging.** Filtering by `eventType` is an extra step that isn't needed when each event type has its own table. Easy to do, but a new habit to build.

**When not to use it.** This pattern makes sense when event types share structural overlap and are frequently queried together. If two event types have completely different fields and are never queried in the same context, consolidating them adds complexity with no benefit.

---

## License

Apache 2.0
