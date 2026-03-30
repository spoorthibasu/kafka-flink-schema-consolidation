package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a scheduled ride reaches completion. */
public class DriverRideCompletedScheduledEvent extends BaseRideEvent {

    private final double fareAmount;
    private final int durationMinutes;
    private final double distanceKm;
    private final long scheduledTime;
    private final int advanceBookingMinutes;

    private DriverRideCompletedScheduledEvent(Map<String, Object> raw) {
        super(raw);
        this.fareAmount            = (Double)  raw.get("fareAmount");
        this.durationMinutes       = (Integer) raw.get("durationMinutes");
        this.distanceKm            = (Double)  raw.get("distanceKm");
        this.scheduledTime         = (Long)    raw.get("scheduledTime");
        this.advanceBookingMinutes = (Integer) raw.get("advanceBookingMinutes");
    }

    public DriverRideCompletedScheduledEvent(long eventTime, String driverId, String rideId,
                                              String cityId, double pickupLat, double pickupLng,
                                              int estimatedDurationMinutes, double estimatedFare,
                                              double fareAmount, int durationMinutes, double distanceKm,
                                              long scheduledTime, int advanceBookingMinutes) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.fareAmount            = fareAmount;
        this.durationMinutes       = durationMinutes;
        this.distanceKm            = distanceKm;
        this.scheduledTime         = scheduledTime;
        this.advanceBookingMinutes = advanceBookingMinutes;
    }

    public static DriverRideCompletedScheduledEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCompletedScheduledEvent(raw);
    }

    public double getFareAmount()         { return fareAmount; }
    public int getDurationMinutes()       { return durationMinutes; }
    public double getDistanceKm()         { return distanceKm; }
    public long getScheduledTime()        { return scheduledTime; }
    public int getAdvanceBookingMinutes() { return advanceBookingMinutes; }
}
