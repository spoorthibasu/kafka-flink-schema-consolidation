package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver picks up the passenger and starts a scheduled ride. */
public class DriverRideStartedScheduledEvent extends BaseRideEvent {

    private final long scheduledTime;
    private final int advanceBookingMinutes;

    private DriverRideStartedScheduledEvent(Map<String, Object> raw) {
        super(raw);
        this.scheduledTime         = (Long)    raw.get("scheduledTime");
        this.advanceBookingMinutes = (Integer) raw.get("advanceBookingMinutes");
    }

    public DriverRideStartedScheduledEvent(long eventTime, String driverId, String rideId,
                                            String cityId, double pickupLat, double pickupLng,
                                            int estimatedDurationMinutes, double estimatedFare,
                                            long scheduledTime, int advanceBookingMinutes) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.scheduledTime         = scheduledTime;
        this.advanceBookingMinutes = advanceBookingMinutes;
    }

    public static DriverRideStartedScheduledEvent fromMap(Map<String, Object> raw) {
        return new DriverRideStartedScheduledEvent(raw);
    }

    public long getScheduledTime()        { return scheduledTime; }
    public int getAdvanceBookingMinutes() { return advanceBookingMinutes; }
}
