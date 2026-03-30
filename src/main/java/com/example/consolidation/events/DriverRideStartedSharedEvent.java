package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver picks up the passenger and starts a shared ride. */
public class DriverRideStartedSharedEvent extends BaseRideEvent {

    private final int passengerCount;
    private final double poolingScore;

    private DriverRideStartedSharedEvent(Map<String, Object> raw) {
        super(raw);
        this.passengerCount = (Integer) raw.get("passengerCount");
        this.poolingScore   = (Double)  raw.get("poolingScore");
    }

    public DriverRideStartedSharedEvent(long eventTime, String driverId, String rideId,
                                         String cityId, double pickupLat, double pickupLng,
                                         int estimatedDurationMinutes, double estimatedFare,
                                         int passengerCount, double poolingScore) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.passengerCount = passengerCount;
        this.poolingScore   = poolingScore;
    }

    public static DriverRideStartedSharedEvent fromMap(Map<String, Object> raw) {
        return new DriverRideStartedSharedEvent(raw);
    }

    public int getPassengerCount() { return passengerCount; }
    public double getPoolingScore() { return poolingScore; }
}
