package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a standard ride reaches completion. */
public class DriverRideCompletedStandardEvent extends BaseRideEvent {

    private final String vehicleClass;
    private final double surgeMultiplier;
    private final double fareAmount;
    private final int durationMinutes;
    private final double distanceKm;

    private DriverRideCompletedStandardEvent(Map<String, Object> raw) {
        super(raw);
        this.vehicleClass    = (String) raw.getOrDefault("vehicleClass", "UberX");
        this.surgeMultiplier = raw.containsKey("surgeMultiplier")
                ? ((Number) raw.get("surgeMultiplier")).doubleValue() : 1.0;
        this.fareAmount      = ((Number) raw.get("fareAmount")).doubleValue();
        this.durationMinutes = ((Number) raw.get("durationMinutes")).intValue();
        this.distanceKm      = ((Number) raw.get("distanceKm")).doubleValue();
    }

    public DriverRideCompletedStandardEvent(long eventTime, String driverId, String rideId,
                                             String cityId, double pickupLat, double pickupLng,
                                             int estimatedDurationMinutes, double estimatedFare,
                                             String vehicleClass, double surgeMultiplier,
                                             double fareAmount, int durationMinutes, double distanceKm) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.vehicleClass    = vehicleClass;
        this.surgeMultiplier = surgeMultiplier;
        this.fareAmount      = fareAmount;
        this.durationMinutes = durationMinutes;
        this.distanceKm      = distanceKm;
    }

    public static DriverRideCompletedStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCompletedStandardEvent(raw);
    }

    public String getVehicleClass()     { return vehicleClass; }
    public double getSurgeMultiplier()  { return surgeMultiplier; }
    public double getFareAmount()       { return fareAmount; }
    public int getDurationMinutes()     { return durationMinutes; }
    public double getDistanceKm()       { return distanceKm; }
}
