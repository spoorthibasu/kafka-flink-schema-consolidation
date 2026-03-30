package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a scheduled ride is cancelled before completion. */
public class DriverRideCancelledScheduledEvent extends BaseRideEvent {

    private final String cancellationReason;
    private final long scheduledTime;
    private final int advanceBookingMinutes;

    private DriverRideCancelledScheduledEvent(Map<String, Object> raw) {
        super(raw);
        this.cancellationReason    = (String)  raw.get("cancellationReason");
        this.scheduledTime         = (Long)    raw.get("scheduledTime");
        this.advanceBookingMinutes = (Integer) raw.get("advanceBookingMinutes");
    }

    public DriverRideCancelledScheduledEvent(long eventTime, String driverId, String rideId,
                                              String cityId, double pickupLat, double pickupLng,
                                              int estimatedDurationMinutes, double estimatedFare,
                                              String cancellationReason, long scheduledTime,
                                              int advanceBookingMinutes) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.cancellationReason    = cancellationReason;
        this.scheduledTime         = scheduledTime;
        this.advanceBookingMinutes = advanceBookingMinutes;
    }

    public static DriverRideCancelledScheduledEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCancelledScheduledEvent(raw);
    }

    public String getCancellationReason() { return cancellationReason; }
    public long getScheduledTime()        { return scheduledTime; }
    public int getAdvanceBookingMinutes() { return advanceBookingMinutes; }
}
