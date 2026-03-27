# Schema Design: Consolidated Event Flattening

This document explains the design decisions behind the consolidated schema approach
used in this repository.

## Why Not One Schema Per Event Type?

The intuitive approach to streaming schema design is one schema per event type.
It is clean, explicit, and easy to reason about for small systems. But it does not
scale well for three reasons.

**Maintenance overhead compounds with schema count.** If you have twelve schemas that
all share the same eight base fields, a change to one of those base fields requires
touching twelve schemas. Each schema is independently maintained, which means
inconsistencies accumulate over time. Field names drift. Types diverge. Required versus
optional semantics conflict.

**Downstream querying becomes fragmented.** When related events live in separate tables,
consumers who need to analyze across event types must write multi-table unions. These
unions are verbose, slow, and error-prone. A simple question like "how many rides were
accepted and then cancelled within ten minutes?" requires a join across two tables.

**Extensibility gets harder, not easier.** Each new event variant requires a new schema,
a new table, new adapters in the processing pipeline, and new joins in downstream queries.
The cost of adding a new event type grows linearly with the existing number of schemas.

## The Consolidated Schema Approach

A consolidated schema inverts the relationship between events and schemas. Instead of
asking "what is the exact shape of this event?", it asks "what is the minimal set of
schemas that covers all events in this logical group?"

For driver ride activity events, the answer is one schema: `DriverRideActivityRecord`.

### Discriminator Fields

Every record carries explicit discriminator fields that identify its variant:

```json
{
  "eventType": "RIDE_COMPLETED",
  "rideType": "SHARED"
}
```

These fields replace the implicit typing that comes from knowing which table a record
came from. Consumers who previously wrote `SELECT * FROM driver_ride_completed_shared`
now write `SELECT * FROM driver_ride_activity WHERE event_type = 'RIDE_COMPLETED' AND ride_type = 'SHARED'`.
The query is slightly longer but the data model is dramatically simpler.

### Nullable Attribute Blocks

Fields that only apply to certain variants are grouped into nullable attribute blocks.

```json
{
  "sharedRideAttributes": {
    "passengerCount": 3,
    "poolingScore": 0.87
  },
  "scheduledRideAttributes": null
}
```

This design keeps the schema self-contained. All the data for a record is in one place,
regardless of which variant it represents. Consumers do not need to join to a separate
table to get type-specific fields.

### Avro Compatibility

Avro's schema evolution rules make this pattern safe over time. Nullable fields with
`"default": null` are backward-compatible additions. This means:

- Adding a new attribute block for a new ride type is backward-compatible
- Existing records read with the new schema see null for the new block
- Existing consumers compiled against the old schema ignore the new block

The only constraint is that type-specific fields must always be nullable. If you have
a field that is required for a certain variant, it still needs to be nullable in the
schema with null as default, and the application layer must enforce that it is populated
for the appropriate variants.

## Tradeoffs

### When Consolidated Schemas Work Well

- Event types share significant structural overlap (more than 70% of fields in common)
- Events are frequently queried together across types
- The number of variants is bounded and well-understood
- You want to minimize the number of downstream tables

### When Separate Schemas May Be Better

- Event types are structurally unrelated (less than 30% of fields in common)
- Events are almost never queried together
- The schema for one event type changes very frequently and independently of others
- Consumer teams prefer strongly typed schemas with no nullable fields

### Null Handling

Consolidated schemas require consumers to be aware of which fields are populated for
which variants. A consumer reading a `RIDE_ACCEPTED` record that tries to access
`fareAmount` will get null. This is expected behavior, not an error, but it requires
documentation and discipline.

The discriminator fields (`eventType`, `rideType`) are the authoritative source of
truth for what is and is not populated in any given record. Always check the discriminator
before accessing type-specific fields.

## Extending the Schema

To add a new ride type (for example, LUXURY):

1. Add `LUXURY` to the `RideType` enum in `RideType.java`
2. Add a `luxuryRideAttributes` nullable field to `DriverRideActivityRecord.avsc`
   with `"default": null`
3. Add a `LuxuryRideAttributes` record type to the Avro schema
4. Add a `LUXURY` case to `ConsolidationAdapter.populateRideTypeAttributeBlock()`
5. Add a `LuxuryRideAttributes` Java class

No new schemas. No new tables. No changes to existing consumers. Existing records
simply have `luxuryRideAttributes: null`, which is correct and expected behavior.
