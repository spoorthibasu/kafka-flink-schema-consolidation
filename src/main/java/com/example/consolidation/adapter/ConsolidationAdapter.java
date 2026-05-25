package com.example.consolidation.adapter;

import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;

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

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        try {
            return MAPPER.readValue(rawEventJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse event: " + rawEventJson, e);
        }
    }
}
