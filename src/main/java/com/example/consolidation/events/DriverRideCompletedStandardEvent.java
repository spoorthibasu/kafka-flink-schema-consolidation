package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a standard ride reaches completion. */
public class DriverRideCompletedStandardEvent extends BaseRideEvent {

    private final double fareAmount;
    private final int durationMinutes;
    private final double distanceKm;

    private DriverRideCompletedStandardEvent(Map<String, Object> raw) {
        super(raw);
        this.fareAmount      = (Double)  raw.get("fareAmount");
        this.durationMinutes = (Integer) raw.get("durationMinutes");
        this.distanceKm      = (Double)  raw.get("distanceKm");
    }

    public DriverRideCompletedStandardEvent(long eventTime, String driverId, String rideId,
                                             String cityId, double pickupLat, double pickupLng,
                                             int estimatedDurationMinutes, double estimatedFare,
                                             double fareAmount, int durationMinutes, double distanceKm) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.fareAmount      = fareAmount;
        this.durationMinutes = durationMinutes;
        this.distanceKm      = distanceKm;
    }

    public static DriverRideCompletedStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCompletedStandardEvent(raw);
    }

    public double getFareAmount()    { return fareAmount; }
    public int getDurationMinutes()  { return durationMinutes; }
    public double getDistanceKm()    { return distanceKm; }
}
