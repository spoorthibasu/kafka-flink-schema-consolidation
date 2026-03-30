package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a standard ride is cancelled before completion. */
public class DriverRideCancelledStandardEvent extends BaseRideEvent {

    private final String vehicleClass;
    private final double surgeMultiplier;
    private final String cancellationReason;

    private DriverRideCancelledStandardEvent(Map<String, Object> raw) {
        super(raw);
        this.vehicleClass       = (String) raw.getOrDefault("vehicleClass", "UberX");
        this.surgeMultiplier    = raw.containsKey("surgeMultiplier")
                ? ((Number) raw.get("surgeMultiplier")).doubleValue() : 1.0;
        this.cancellationReason = (String) raw.get("cancellationReason");
    }

    public DriverRideCancelledStandardEvent(long eventTime, String driverId, String rideId,
                                             String cityId, double pickupLat, double pickupLng,
                                             int estimatedDurationMinutes, double estimatedFare,
                                             String vehicleClass, double surgeMultiplier,
                                             String cancellationReason) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.vehicleClass       = vehicleClass;
        this.surgeMultiplier    = surgeMultiplier;
        this.cancellationReason = cancellationReason;
    }

    public static DriverRideCancelledStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCancelledStandardEvent(raw);
    }

    public String getVehicleClass()       { return vehicleClass; }
    public double getSurgeMultiplier()    { return surgeMultiplier; }
    public String getCancellationReason() { return cancellationReason; }
}
