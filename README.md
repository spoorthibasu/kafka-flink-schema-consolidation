# Kafka-Flink Schema Consolidation

A reference implementation of the **consolidated event flattening pattern** for Apache Kafka and Apache Flink pipelines.

This pattern solves one of the most common scaling problems in real-time streaming architectures: **schema proliferation**, where one-to-one event-to-schema mapping leads to dozens of fragmented schemas, dozens of downstream tables, and compounding maintenance overhead.

---

## The Problem

When you map each Kafka event type to its own Flink schema and downstream table, things look clean at first. But as your event landscape grows, the cost compounds:

- A change to a shared field requires updating N schemas
- Downstream consumers must union N tables to query related events
- Schema drift accumulates across independently maintained schemas
- Adding a new event variant means a new schema, a new table, new adapters

This repository demonstrates how to replace N fragmented schemas with a single **consolidated schema** using discriminator-based routing and nullable type-specific attribute blocks.

### Before: One Schema Per Event Type

```
DriverRideAcceptedStandardEvent  -->  driver_ride_accepted_standard table
DriverRideAcceptedSharedEvent    -->  driver_ride_accepted_shared table
DriverRideAcceptedScheduledEvent -->  driver_ride_accepted_scheduled table
DriverRideCompletedStandardEvent -->  driver_ride_completed_standard table
DriverRideCompletedSharedEvent   -->  driver_ride_completed_shared table
... (N schemas, N tables)
```

### After: One Consolidated Schema

```
All driver ride events  -->  DriverRideActivityRecord  -->  driver_ride_activity table
```

---

## The Pattern

### 1. Discriminator Fields

Every record in the consolidated schema carries explicit discriminator fields that identify the event variant:

```json
{
  "eventType": "RIDE_COMPLETED",
  "rideType": "SHARED"
}
```

Consumers filter on these fields instead of querying different tables.

### 2. Nullable Type-Specific Attribute Blocks

Fields that only apply to certain event variants are grouped into nullable attribute blocks. Only the relevant block is populated for each record. All others are null.

```json
{
  "sharedRideAttributes": {
    "passengerCount": 3,
    "poolingScore": 0.87
  },
  "scheduledRideAttributes": null
}
```

### 3. Flink Adapter Layer

A Flink adapter sits between the Kafka consumer and schema serialization. It:

1. Identifies the incoming event type
2. Populates shared fields common to all variants
3. Sets discriminator fields
4. Populates the appropriate attribute block
5. Sets all other attribute blocks to null
6. Serializes as an Avro record

---

## Architecture

```
Kafka Topics
    |
    | (raw events: DriverRideAcceptedStandard, DriverRideAcceptedShared, etc.)
    v
Flink Job
    |
    +--> KafkaEventConsumer
    |         |
    |         v
    |    EventTypeRouter  (identifies event type from topic/metadata)
    |         |
    |         v
    |    ConsolidationAdapter  (maps to DriverRideActivityRecord)
    |         |
    |         v
    |    AvroSerializer
    |
    v
S3 / Data Lake
    |
    v
driver_ride_activity  (single Parquet/Iceberg table)
```

---

## Repository Structure

```
kafka-flink-schema-consolidation/
├── src/
│   └── main/
│       ├── avro/
│       │   ├── fragmented/                    # Before: separate schemas
│       │   │   ├── DriverRideAcceptedStandard.avsc
│       │   │   ├── DriverRideAcceptedShared.avsc
│       │   │   └── DriverRideAcceptedScheduled.avsc
│       │   └── consolidated/                  # After: single schema
│       │       └── DriverRideActivityRecord.avsc
│       └── java/com/example/consolidation/
│           ├── model/
│           │   ├── RideEventType.java         # Discriminator enum
│           │   └── RideType.java              # Ride type enum
│           ├── adapter/
│           │   ├── ConsolidationAdapter.java  # Core pattern implementation
│           │   └── EventTypeRouter.java       # Routes events to correct adapter
│           └── job/
│               └── RideEventConsolidationJob.java  # Flink job entry point
├── docs/
│   └── schema-design.md                       # Design decisions and tradeoffs
├── pom.xml
└── README.md
```

---

## Avro Schema: Before vs After

### Before (3 of N fragmented schemas)

`DriverRideAcceptedStandard.avsc` — 8 fields  
`DriverRideAcceptedShared.avsc` — 10 fields (8 shared + 2 shared-ride-specific)  
`DriverRideAcceptedScheduled.avsc` — 10 fields (8 shared + 2 scheduled-ride-specific)

95% of fields are duplicated across schemas.

### After (1 consolidated schema)

`DriverRideActivityRecord.avsc` — all variants in one schema, type-specific fields in nullable blocks.

See `src/main/avro/` for the full schema definitions.

---

## Key Design Decisions

### Why Avro?

Avro supports nullable fields with default values, which is essential for the nullable attribute block pattern. Adding a new attribute block for a new ride type is a **backward-compatible schema change** — existing records default the new field to null, and existing consumers continue to work without modification.

### Why Discriminator Fields Instead of Union Types?

Avro union types (`["null", "RecordType"]`) can represent nullable records but make queries harder. Explicit discriminator enums (`eventType`, `rideType`) make filtering straightforward in SQL, Spark, and Flink:

```sql
SELECT * FROM driver_ride_activity
WHERE event_type = 'RIDE_COMPLETED'
AND ride_type = 'SHARED'
```

### Extensibility

Adding a new ride type (e.g., `LUXURY`) requires:
1. Adding `LUXURY` to the `RideType` enum
2. Adding a nullable `luxuryRideAttributes` block to the schema (backward-compatible)
3. Adding a handler in `ConsolidationAdapter`

No new schemas. No new tables. No changes to existing consumers.

---

## Getting Started

### Prerequisites

- Java 11+
- Apache Flink 1.17+
- Apache Kafka 3.x
- Maven 3.8+

### Build

```bash
mvn clean package
```

### Run the Flink Job

```bash
flink run -c com.example.consolidation.job.RideEventConsolidationJob \
  target/kafka-flink-schema-consolidation-1.0.jar \
  --kafka-bootstrap-servers localhost:9092 \
  --input-topics driver-ride-accepted,driver-ride-completed,driver-ride-cancelled \
  --output-path s3://your-bucket/driver_ride_activity \
  --checkpoint-interval 60000
```

---

## Schema Evolution Example

When a new attribute block is added to `DriverRideActivityRecord`, the change is backward-compatible because the new field is nullable with a default of null:

```json
{
  "name": "luxuryRideAttributes",
  "type": ["null", {
    "type": "record",
    "name": "LuxuryRideAttributes",
    "fields": [
      {"name": "vehicleClass", "type": "string"},
      {"name": "amenitiesIncluded", "type": {"type": "array", "items": "string"}}
    ]
  }],
  "default": null
}
```

Existing Flink jobs reading the old schema version continue to work. The new field is simply absent from older records.

---

## Related Reading

- [The Schema Proliferation Problem in Kafka-Flink Pipelines](https://medium.com) — companion article explaining the pattern in depth
- [Apache Avro Schema Evolution](https://avro.apache.org/docs/current/spec.html#Schema+Resolution)
- [Apache Flink Documentation](https://nightlies.apache.org/flink/flink-docs-stable/)

---

## Contributing

Contributions welcome. If you have encountered schema proliferation in a different domain (IoT, e-commerce, healthcare) and want to add an example, please open a pull request.

---

## License

Apache 2.0 — see [LICENSE](LICENSE) for details.
