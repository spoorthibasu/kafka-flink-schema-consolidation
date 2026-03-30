# Schema Design: Consolidated Event Flattening

Design decisions behind the consolidated schema approach used in this repository.

## Why not one schema per event type?

One schema per event type is clean and easy to reason about for small systems. It stops working well as the schema count grows.

**Maintenance overhead compounds.** If twelve schemas share the same eight base fields, changing one of those fields means touching twelve schemas. Maintained independently, they drift: field names differ, types diverge, required/optional semantics conflict.

**Querying becomes fragmented.** Related events spread across separate tables force consumers to write multi-table unions. A question like "how many rides were accepted and then cancelled within ten minutes?" needs a join across two tables. At twelve schemas, a simple cross-variant query becomes a project.

**Adding new variants gets more expensive, not cheaper.** Each new event type needs a new schema, a new table, new adapter code, and new joins in downstream queries. The cost grows linearly with the existing schema count.

## The consolidated schema approach

A consolidated schema asks a different question: what is the minimal set of schemas that covers all events in this logical group?

For driver ride activity events, the answer is one schema: `DriverRideActivityRecord`.

### Discriminator fields

Every record carries explicit discriminator fields:

```json
{
  "eventType": "COMPLETED",
  "rideType": "SHARED"
}
```

Consumers who previously wrote `SELECT * FROM driver_ride_completed_shared` now write:

```sql
SELECT * FROM driver_ride_activity
WHERE event_type = 'COMPLETED' AND ride_type = 'SHARED'
```

The query is slightly longer but the data model is much simpler.

### Nullable attribute blocks

Fields specific to certain variants are grouped into nullable nested records.

```json
{
  "sharedRideAttributes": {
    "passengerCount": 3,
    "poolingScore": 0.87
  },
  "scheduledRideAttributes": null
}
```

All data for a record is in one place, regardless of variant. Consumers don't need to join a separate table for type-specific fields.

### Avro compatibility

Nullable fields with `"default": null` are backward-compatible additions. This means:

- Adding a new attribute block for a new ride type is safe
- Existing records read with the new schema see null for the new block
- Consumers compiled against the old schema ignore the new block

Type-specific fields must always be nullable. If a field is required for a certain variant, it still needs a null default in the schema. The application layer enforces that it is populated for the right variants.

## Trade-offs

### When consolidated schemas work well

- Event types share significant structural overlap (70%+ of fields in common)
- Events are frequently queried together across types
- The number of variants is bounded and well-understood
- Fewer downstream tables is a goal

### When separate schemas may be better

- Event types are structurally unrelated (less than 30% of fields in common)
- Events are almost never queried together
- One event type's schema changes very frequently and independently of others
- Consumer teams strongly prefer no nullable fields

### Null handling

Consumers need to know which fields are populated for which variants. A consumer reading an `ACCEPTED` record that accesses `fareAmount` will get null. That is expected, not an error, but it requires documentation.

Always check the discriminator fields (`eventType`, `rideType`) before accessing type-specific fields.

## Extending the schema

To add a new ride type (for example, LUXURY):

1. Add `LUXURY` to `RideType.java`
2. Add a `luxuryRideAttributes` nullable field to `DriverRideActivityRecord.avsc` with `"default": null`
3. Add a `LuxuryRideAttributes` record type to the Avro schema
4. Create `LuxuryRide*Adapter` classes implementing `RecordAdapter`
5. Register them in `AdapterRegistry.withAllAdapters()`, one call per event type
6. Add a `LuxuryRideAttributes` Java class

No new schemas, no new tables, no changes to existing consumers. Existing records have `luxuryRideAttributes: null`, which is correct behavior.
