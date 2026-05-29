package com.example.consolidation.adapter;

import java.util.Map;

/**
 * A ride event straight off Kafka, before it's routed to a typed adapter. It holds
 * the parsed JSON as a map and exposes the two discriminators the router reads.
 *
 * A real pipeline would deserialize into a generated Avro type instead; the map
 * keeps this example dependency-free.
 */
public class RawRideEvent {

    private Map<String, Object> fields;

    public RawRideEvent() {
    }

    public RawRideEvent(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public String getEventType() {
        return fields == null ? null : (String) fields.get("eventType");
    }

    public String getRideType() {
        return fields == null ? null : (String) fields.get("rideType");
    }
}
