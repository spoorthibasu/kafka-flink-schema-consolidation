package com.example.consolidation.adapter;

import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.HashMap;
import java.util.Map;

/**
 * Flink framework integration layer for schema consolidation.
 *
 * Reads raw Kafka events (JSON strings), extracts the eventType and rideType
 * discriminator fields, then delegates to the AdapterRegistry to route each
 * event to its typed RecordAdapter. The resulting DriverRideActivityRecord is
 * written downstream to the consolidated Iceberg table.
 *
 * All transformation logic lives in the individual RecordAdapter implementations,
 * which have no Flink dependency and can be unit tested without framework setup.
 */
public class ConsolidationAdapter implements MapFunction<String, DriverRideActivityRecord> {

    private final AdapterRegistry adapterRegistry;

    public ConsolidationAdapter(AdapterRegistry adapterRegistry) {
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public DriverRideActivityRecord map(String rawEventJson) throws Exception {
        Map<String, Object> rawEvent = parseEvent(rawEventJson);
        EventType eventType = resolveEventType(rawEvent);
        RideType rideType = resolveRideType(rawEvent);
        return adapterRegistry.adapt("default", eventType, rideType, rawEvent);
    }

    private EventType resolveEventType(Map<String, Object> rawEvent) {
        String typeStr = (String) rawEvent.getOrDefault("eventType", "ACCEPTED");
        try {
            return EventType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EventType.ACCEPTED;
        }
    }

    private RideType resolveRideType(Map<String, Object> rawEvent) {
        String typeStr = (String) rawEvent.getOrDefault("rideType", "STANDARD");
        try {
            return RideType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RideType.STANDARD;
        }
    }

    private Map<String, Object> parseEvent(String rawEventJson) {
        // In production: use Jackson ObjectMapper or Avro deserializer.
        // This stub simulates a parsed event for the reference implementation.
        Map<String, Object> event = new HashMap<>();
        event.put("eventTime", System.currentTimeMillis());
        event.put("driverId", "driver-123");
        event.put("rideId", "ride-456");
        event.put("cityId", "NYC");
        event.put("pickupLat", 40.7128);
        event.put("pickupLng", -74.0060);
        event.put("estimatedDurationMinutes", 12);
        event.put("estimatedFare", 18.50);
        return event;
    }
}
