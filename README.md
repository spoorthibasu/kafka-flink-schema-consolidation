# Kafka-Flink Schema Consolidation

A reference implementation of a **production-oriented pattern** for addressing schema proliferation in Apache Kafka and Apache Flink pipelines.

This repository demonstrates how to replace fragmented, one-to-one event schemas with a **consolidated schema** using discriminator-based routing and nullable attribute blocks, improving scalability, maintainability, and downstream usability.

This pattern is based on real-world streaming systems and is applicable to large-scale data infrastructure handling high-volume event streams.

---

## The Problem

When you map each Kafka event type to its own Flink schema and downstream table, things look clean at first. But as your event landscape grows, the cost compounds:

- A change to a shared field requires updating N schemas  
- Downstream consumers must union N tables to query related events  
- Schema drift accumulates across independently maintained schemas  
- Adding a new event variant introduces new schemas, tables, and transformation logic  

This repository demonstrates how to replace N fragmented schemas with a single **consolidated schema** using discriminator-based routing and nullable type-specific attribute blocks.

---

## Impact

In production systems, schema proliferation introduces significant operational overhead:

- Query complexity increases due to multi-table joins and unions  
- Schema changes must be replicated across multiple definitions  
- Data inconsistencies emerge due to schema drift  
- Onboarding new consumers becomes slower and error-prone  

By consolidating schemas into a unified structure, this pattern:

- Reduces query complexity to simple filter-based access  
- Centralizes schema evolution into a single definition  
- Improves data consistency across event types  
- Enables faster downstream analytics and development workflows  

---

## Before vs After

### Before: One Schema Per Event Type

```
DriverRideAcceptedStandardEvent  -->  driver_ride_accepted_standard table
DriverRideAcceptedSharedEvent    -->  driver_ride_accepted_shared table
DriverRideAcceptedScheduledEvent -->  driver_ride_accepted_scheduled table
DriverRideCompletedStandardEvent -->  driver_ride_completed_standard table
DriverRideCompletedSharedEvent   -->  driver_ride_completed_shared table
... (N schemas, N tables)
```

---

### After: One Consolidated Schema

```
All driver ride events  -->  DriverRideActivityRecord  -->  driver_ride_activity table
```

---

## The Pattern

### 1. Discriminator Fields

Every record carries explicit identifiers:

```json
{
  "eventType": "RIDE_COMPLETED",
  "rideType": "SHARED"
}
```

Consumers filter using these fields instead of querying multiple tables.

---

### 2. Nullable Type-Specific Attribute Blocks

Variant-specific fields are grouped into nullable structures:

```json
{
  "sharedRideAttributes": {
    "passengerCount": 3,
    "poolingScore": 0.87
  },
  "scheduledRideAttributes": null
}
```

Only relevant blocks are populated per record.

---

### 3. Flink Adapter Layer

A Flink transformation layer performs consolidation:

1. Identify event type from Kafka metadata  
2. Populate shared fields  
3. Set discriminator fields  
4. Populate relevant attribute block  
5. Set all other blocks to null  
6. Serialize into unified schema  

---

## Architecture

```
Kafka Topics
    |
    | (raw events: DriverRideAcceptedStandard, etc.)
    v
Flink Job
    |
    +--> KafkaEventConsumer
    |         |
    |         v
    |    EventTypeRouter
    |         |
    |         v
    |    ConsolidationAdapter
    |         |
    |         v
    |    AvroSerializer
    |
    v
S3 / Data Lake
    |
    v
driver_ride_activity (single table)
```

---

## Repository Structure

```
kafka-flink-schema-consolidation/
├── src/
│   └── main/
│       ├── avro/
│       │   ├── fragmented/
│       │   └── consolidated/
│       └── java/com/example/consolidation/
│           ├── model/
│           ├── adapter/
│           └── job/
├── docs/
├── pom.xml
└── README.md
```

---

## Avro Schema: Before vs After

### Before
Multiple schemas with duplicated fields across variants.

### After
Single consolidated schema with shared fields and nullable attribute blocks.

---

## Key Design Decisions

### Why Avro?

- Supports nullable fields with defaults  
- Enables backward-compatible schema evolution  
- Well-supported in Kafka + Flink ecosystems  

---

### Why Discriminator Fields Instead of Union Types?

Explicit discriminator fields:

- simplify querying  
- improve readability  
- work well with SQL engines  

```sql
SELECT * FROM driver_ride_activity
WHERE event_type = 'RIDE_COMPLETED'
AND ride_type = 'SHARED'
```

---

### Extensibility

Adding a new event type requires:

1. Updating enum  
2. Adding nullable attribute block  
3. Updating adapter logic  

No new schemas or tables are required.

---

## Trade-offs

While consolidated schemas improve usability, they introduce trade-offs:

- Wider schemas may increase storage footprint  
- Serialization overhead may increase  
- Schema governance becomes more important  

This approach is most effective when event types share structural overlap and are frequently queried together.

---

## When Not to Use This Pattern

This pattern may not be suitable when:

- Event types are structurally unrelated  
- Events are rarely queried together  
- Strict domain isolation is required  

In such cases, separate schemas may provide better clarity.

---

## Getting Started

### Prerequisites

- Java 11+  
- Apache Flink 1.17+  
- Apache Kafka 3.x  
- Maven 3.8+  

---

### Build

```bash
mvn clean package
```

---

### Run the Flink Job

```bash
flink run -c com.example.consolidation.job.RideEventConsolidationJob \
  target/kafka-flink-schema-consolidation-1.0.jar \
  --kafka-bootstrap-servers localhost:9092 \
  --input-topics driver-ride-accepted,driver-ride-completed \
  --output-path s3://your-bucket/driver_ride_activity \
  --checkpoint-interval 60000
```

---

## Schema Evolution Example

Adding a new attribute block remains backward-compatible:

```json
{
  "name": "luxuryRideAttributes",
  "type": ["null", {
    "type": "record",
    "name": "LuxuryRideAttributes",
    "fields": [
      {"name": "vehicleClass", "type": "string"}
    ]
  }],
  "default": null
}
```

---

## Related Reading

- Medium article (companion piece)  
- Apache Avro docs  
- Apache Flink docs  

---

## Contributing

Contributions welcome. Examples from other domains (IoT, finance, healthcare) are encouraged.

---

## License

Apache 2.0
