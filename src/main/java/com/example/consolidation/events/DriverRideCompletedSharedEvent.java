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
        this.fareAmount      = (Double)  raw.get("fareAmount");
        this.durationMinutes = (Integer) raw.get("durationMinutes");
        this.distanceKm      = (Double)  raw.get("distanceKm");
        this.passengerCount  = (Integer) raw.get("passengerCount");
        this.poolingScore    = (Double)  raw.get("poolingScore");
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
