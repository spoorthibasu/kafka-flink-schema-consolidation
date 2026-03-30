package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver accepts a standard (single-passenger) ride request. */
public class DriverRideAcceptedStandardEvent extends BaseRideEvent {

    private final String vehicleClass;
    private final double surgeMultiplier;

    private DriverRideAcceptedStandardEvent(Map<String, Object> raw) {
        super(raw);
        this.vehicleClass    = (String) raw.getOrDefault("vehicleClass", "UberX");
        this.surgeMultiplier = raw.containsKey("surgeMultiplier")
                ? ((Number) raw.get("surgeMultiplier")).doubleValue() : 1.0;
    }

    public DriverRideAcceptedStandardEvent(long eventTime, String driverId, String rideId,
                                            String cityId, double pickupLat, double pickupLng,
                                            int estimatedDurationMinutes, double estimatedFare,
                                            String vehicleClass, double surgeMultiplier) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.vehicleClass    = vehicleClass;
        this.surgeMultiplier = surgeMultiplier;
    }

    public static DriverRideAcceptedStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideAcceptedStandardEvent(raw);
    }

    public String getVehicleClass()     { return vehicleClass; }
    public double getSurgeMultiplier()  { return surgeMultiplier; }
}
