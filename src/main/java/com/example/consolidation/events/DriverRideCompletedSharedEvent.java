package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a shared ride reaches completion. */
public class DriverRideCompletedSharedEvent extends BaseRideEvent {

    private final double fareAmount;
    private final int durationMinutes;
    private final double distanceKm;
    private final int passengerCount;
    private final double poolingScore;

    private DriverRideCompletedSharedEvent(Map<String, Object> raw) {
        super(raw);
        this.fareAmount      = ((Number) raw.get("fareAmount")).doubleValue();
        this.durationMinutes = ((Number) raw.get("durationMinutes")).intValue();
        this.distanceKm      = ((Number) raw.get("distanceKm")).doubleValue();
        this.passengerCount  = ((Number) raw.get("passengerCount")).intValue();
        this.poolingScore    = ((Number) raw.get("poolingScore")).doubleValue();
    }

    public DriverRideCompletedSharedEvent(long eventTime, String driverId, String rideId,
                                           String cityId, double pickupLat, double pickupLng,
                                           int estimatedDurationMinutes, double estimatedFare,
                                           double fareAmount, int durationMinutes, double distanceKm,
                                           int passengerCount, double poolingScore) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.fareAmount      = fareAmount;
        this.durationMinutes = durationMinutes;
        this.distanceKm      = distanceKm;
        this.passengerCount  = passengerCount;
        this.poolingScore    = poolingScore;
    }

    public static DriverRideCompletedSharedEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCompletedSharedEvent(raw);
    }

    public double getFareAmount()    { return fareAmount; }
    public int getDurationMinutes()  { return durationMinutes; }
    public double getDistanceKm()    { return distanceKm; }
    public int getPassengerCount()   { return passengerCount; }
    public double getPoolingScore()  { return poolingScore; }
}
