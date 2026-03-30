package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideAcceptedSharedEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates that RecordAdapter implementations are pure Java with no Flink dependency.
 * No framework setup, no Kafka, no cluster — just a plain JUnit test.
 */
class SharedRideAcceptedAdapterTest {

    private SharedRideAcceptedAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SharedRideAcceptedAdapter();
    }

    @Test
    void adapt_setsDiscriminatorFields() {
        DriverRideAcceptedSharedEvent event = sampleEvent();

        ConsolidatedRecord record = adapter.adapt("org-1", event);

        assertEquals(RideEventType.RIDE_ACCEPTED, record.getEventType());
        assertEquals(RideType.SHARED, record.getRideType());
    }

    @Test
    void adapt_copiesSharedFields() {
        DriverRideAcceptedSharedEvent event = sampleEvent();

        ConsolidatedRecord record = adapter.adapt("org-1", event);

        assertEquals(1_700_000_000_000L, record.getEventTime());
        assertEquals("driver-4821", record.getDriverId());
        assertEquals("ride-7701", record.getRideId());
        assertEquals("NYC", record.getCityId());
        assertEquals(40.7128, record.getPickupLat(), 0.0001);
        assertEquals(-74.0060, record.getPickupLng(), 0.0001);
        assertEquals(12, record.getEstimatedDurationMinutes());
        assertEquals(18.50, record.getEstimatedFare(), 0.001);
    }

    @Test
    void adapt_populatesSharedRideAttributeBlock() {
        DriverRideAcceptedSharedEvent event = sampleEvent();

        ConsolidatedRecord record = adapter.adapt("org-1", event);

        assertNotNull(record.getSharedRideAttributes());
        assertEquals(2, record.getSharedRideAttributes().getPassengerCount());
        assertEquals(0.87, record.getSharedRideAttributes().getPoolingScore(), 0.001);
    }

    @Test
    void adapt_leavesOtherAttributeBlocksNull() {
        DriverRideAcceptedSharedEvent event = sampleEvent();

        ConsolidatedRecord record = adapter.adapt("org-1", event);

        assertNull(record.getScheduledRideAttributes(),
                "scheduledRideAttributes must be null for SHARED rides");
    }

    @Test
    void adapt_leavesCompletionFieldsNull() {
        DriverRideAcceptedSharedEvent event = sampleEvent();

        ConsolidatedRecord record = adapter.adapt("org-1", event);

        assertNull(record.getFareAmount(),       "fareAmount must be null for ACCEPTED events");
        assertNull(record.getDurationMinutes(),  "durationMinutes must be null for ACCEPTED events");
        assertNull(record.getDistanceKm(),       "distanceKm must be null for ACCEPTED events");
        assertNull(record.getCancellationReason(), "cancellationReason must be null for ACCEPTED events");
    }

    private DriverRideAcceptedSharedEvent sampleEvent() {
        return new DriverRideAcceptedSharedEvent(
                1_700_000_000_000L,  // eventTime
                "driver-4821",        // driverId
                "ride-7701",          // rideId
                "NYC",                // cityId
                40.7128,              // pickupLat
                -74.0060,             // pickupLng
                12,                   // estimatedDurationMinutes
                18.50,                // estimatedFare
                2,                    // passengerCount
                0.87                  // poolingScore
        );
    }
}
