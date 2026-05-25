package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver accepts a pre-scheduled ride request. */
public class DriverRideAcceptedScheduledEvent extends BaseRideEvent {

    private final long scheduledTime;
    private final int advanceBookingMinutes;

    private DriverRideAcceptedScheduledEvent(Map<String, Object> raw) {
        super(raw);
        this.scheduledTime          = ((Number) raw.get("scheduledTime")).longValue();
        this.advanceBookingMinutes  = ((Number) raw.get("advanceBookingMinutes")).intValue();
    }

    public DriverRideAcceptedScheduledEvent(long eventTime, String driverId, String rideId,
                                             String cityId, double pickupLat, double pickupLng,
                                             int estimatedDurationMinutes, double estimatedFare,
                                             long scheduledTime, int advanceBookingMinutes) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.scheduledTime         = scheduledTime;
        this.advanceBookingMinutes = advanceBookingMinutes;
    }

    public static DriverRideAcceptedScheduledEvent fromMap(Map<String, Object> raw) {
        return new DriverRideAcceptedScheduledEvent(raw);
    }

    public long getScheduledTime()         { return scheduledTime; }
    public int getAdvanceBookingMinutes()  { return advanceBookingMinutes; }
}
