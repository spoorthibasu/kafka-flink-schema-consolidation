package com.example.consolidation.adapter;

import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Map;

/**
 * Flink framework integration layer for schema consolidation.
 *
 * This class is intentionally thin. It has exactly two responsibilities:
 *   1. Read the discriminator fields (_eventType, _rideType) added by EventTypeRouter.
 *   2. Delegate to the AdapterRegistry, which routes to the correct typed adapter.
 *
 * All transformation logic lives in the individual RecordAdapter implementations,
 * which have no Flink dependency and can be unit tested without framework setup.
 * This class only wires Flink's MapFunction contract to the registry.
 */
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

    private RideEventType resolveEventType(Map<String, Object> rawEvent) {
        String typeStr = (String) rawEvent.getOrDefault("_eventType", "RIDE_ACCEPTED");
        try {
            return RideEventType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RideEventType.RIDE_ACCEPTED;
        }
    }

    private RideType resolveRideType(Map<String, Object> rawEvent) {
        String typeStr = (String) rawEvent.getOrDefault("_rideType", "STANDARD");
        try {
            return RideType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RideType.STANDARD;
        }
    }
}
