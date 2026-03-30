package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver picks up the passenger and starts a standard ride. */
public class DriverRideStartedStandardEvent extends BaseRideEvent {

    private final String vehicleClass;
    private final double surgeMultiplier;

    private DriverRideStartedStandardEvent(Map<String, Object> raw) {
        super(raw);
        this.vehicleClass    = (String) raw.getOrDefault("vehicleClass", "UberX");
        this.surgeMultiplier = raw.containsKey("surgeMultiplier")
                ? ((Number) raw.get("surgeMultiplier")).doubleValue() : 1.0;
    }

    public DriverRideStartedStandardEvent(long eventTime, String driverId, String rideId,
                                           String cityId, double pickupLat, double pickupLng,
                                           int estimatedDurationMinutes, double estimatedFare,
                                           String vehicleClass, double surgeMultiplier) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.vehicleClass    = vehicleClass;
        this.surgeMultiplier = surgeMultiplier;
    }

    public static DriverRideStartedStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideStartedStandardEvent(raw);
    }

    public String getVehicleClass()     { return vehicleClass; }
    public double getSurgeMultiplier()  { return surgeMultiplier; }
}
