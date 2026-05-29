package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a scheduled ride reaches completion. */
public class DriverRideCompletedScheduledEvent extends BaseRideEvent {

    private final double fareAmount;
    private final int durationMins;
    private final double distanceKm;
    private final long scheduledTime;
    private final int advanceBookingMinutes;

    private DriverRideCompletedScheduledEvent(Map<String, Object> raw) {
        super(raw);
        this.fareAmount            = ((Number) raw.get("fareAmount")).doubleValue();
        this.durationMins       = ((Number) raw.get("durationMins")).intValue();
        this.distanceKm            = ((Number) raw.get("distanceKm")).doubleValue();
        this.scheduledTime         = ((Number) raw.get("scheduledTime")).longValue();
        this.advanceBookingMinutes = ((Number) raw.get("advanceBookingMinutes")).intValue();
    }

    public DriverRideCompletedScheduledEvent(long eventTime, String driverId, String rideId,
                                              String cityId, double pickupLat, double pickupLng,
                                              int estimatedDurationMinutes, double estimatedFare,
                                              double fareAmount, int durationMins, double distanceKm,
                                              long scheduledTime, int advanceBookingMinutes) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.fareAmount            = fareAmount;
        this.durationMins       = durationMins;
        this.distanceKm            = distanceKm;
        this.scheduledTime         = scheduledTime;
        this.advanceBookingMinutes = advanceBookingMinutes;
    }

    public static DriverRideCompletedScheduledEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCompletedScheduledEvent(raw);
    }

    public double getFareAmount()         { return fareAmount; }
    public int getDurationMins()       { return durationMins; }
    public double getDistanceKm()         { return distanceKm; }
    public long getScheduledTime()        { return scheduledTime; }
    public int getAdvanceBookingMinutes() { return advanceBookingMinutes; }
}
