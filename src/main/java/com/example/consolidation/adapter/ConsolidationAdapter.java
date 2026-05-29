package com.example.consolidation.adapter;

import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Map;

/**
 * Flink map function that routes each event to its variant adapter.
 *
 * It reads the eventType and rideType discriminators, then hands the raw event to
 * the registry, which picks the matching RecordAdapter. The adapters themselves
 * have no Flink dependency, so all transformation logic stays unit-testable.
 */
public class ConsolidationAdapter implements MapFunction<RawRideEvent, DriverRideActivityRecord> {

    private final AdapterRegistry adapterRegistry;

    public ConsolidationAdapter(AdapterRegistry adapterRegistry) {
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public DriverRideActivityRecord map(RawRideEvent event) {
        Map<String, Object> rawEvent = event.getFields();
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
}
