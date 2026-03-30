package com.example.consolidation.adapter;

import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that AdapterRegistry routes to the correct adapter for every
 * (RideEventType, RideType) combination and that adding an unknown combination
 * fails fast rather than silently producing wrong output.
 */
class AdapterRegistryTest {

    private final AdapterRegistry registry = AdapterRegistry.withAllAdapters();

    @Test
    void allTwelveCombinationsAreRegistered() {
        for (RideEventType eventType : RideEventType.values()) {
            for (RideType rideType : RideType.values()) {
                Map<String, Object> raw = baseRawEvent();
                raw.put("_eventType", eventType.name());
                raw.put("_rideType", rideType.name());
                addVariantFields(raw, eventType, rideType);

                ConsolidatedRecord record = assertDoesNotThrow(
                        () -> registry.adapt("org-1", eventType, rideType, raw),
                        "Missing adapter for " + eventType + "/" + rideType);

                assertEquals(eventType, record.getEventType());
                assertEquals(rideType, record.getRideType());
            }
        }
    }

    @Test
    void unknownCombinationThrowsIllegalArgumentException() {
        // Simulate a new event type not yet registered
        AdapterRegistry emptyRegistry = new AdapterRegistry();
        assertThrows(IllegalArgumentException.class, () ->
                emptyRegistry.adapt("org-1", RideEventType.RIDE_ACCEPTED, RideType.STANDARD, baseRawEvent()));
    }

    @Test
    void sharedRideAccepted_populatesAttributeBlock() {
        Map<String, Object> raw = baseRawEvent();
        raw.put("passengerCount", 3);
        raw.put("poolingScore", 0.91);

        ConsolidatedRecord record = registry.adapt("org-1",
                RideEventType.RIDE_ACCEPTED, RideType.SHARED, raw);

        assertNotNull(record.getSharedRideAttributes());
        assertEquals(3, record.getSharedRideAttributes().getPassengerCount());
        assertNull(record.getScheduledRideAttributes());
    }

    @Test
    void completedRide_populatesFareFields() {
        Map<String, Object> raw = baseRawEvent();
        raw.put("fareAmount", 22.75);
        raw.put("durationMinutes", 18);
        raw.put("distanceKm", 7.3);

        ConsolidatedRecord record = registry.adapt("org-1",
                RideEventType.RIDE_COMPLETED, RideType.STANDARD, raw);

        assertEquals(22.75, record.getFareAmount(), 0.001);
        assertEquals(18, record.getDurationMinutes());
        assertEquals(7.3, record.getDistanceKm(), 0.001);
    }

    private Map<String, Object> baseRawEvent() {
        Map<String, Object> raw = new HashMap<>();
        raw.put("eventTime", System.currentTimeMillis());
        raw.put("driverId", "driver-001");
        raw.put("rideId", "ride-001");
        raw.put("cityId", "NYC");
        raw.put("pickupLat", 40.7128);
        raw.put("pickupLng", -74.0060);
        raw.put("estimatedDurationMinutes", 10);
        raw.put("estimatedFare", 15.00);
        return raw;
    }

    private void addVariantFields(Map<String, Object> raw,
                                   RideEventType eventType, RideType rideType) {
        if (rideType == RideType.SHARED) {
            raw.put("passengerCount", 2);
            raw.put("poolingScore", 0.80);
        }
        if (rideType == RideType.SCHEDULED) {
            raw.put("scheduledTime", System.currentTimeMillis() + 3_600_000L);
            raw.put("advanceBookingMinutes", 60);
        }
        if (eventType == RideEventType.RIDE_COMPLETED) {
            raw.put("fareAmount", 20.00);
            raw.put("durationMinutes", 14);
            raw.put("distanceKm", 5.5);
        }
        if (eventType == RideEventType.RIDE_CANCELLED) {
            raw.put("cancellationReason", "DRIVER_CANCELLED");
        }
    }
}
