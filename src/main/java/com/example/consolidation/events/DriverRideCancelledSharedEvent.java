package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a shared ride is cancelled before completion. */
public class DriverRideCancelledSharedEvent extends BaseRideEvent {

    private final String cancellationReason;
    private final int passengerCount;
    private final double poolingScore;

    private DriverRideCancelledSharedEvent(Map<String, Object> raw) {
        super(raw);
        this.cancellationReason = (String)  raw.get("cancellationReason");
        this.passengerCount     = (Integer) raw.get("passengerCount");
        this.poolingScore       = (Double)  raw.get("poolingScore");
    }

    public DriverRideCancelledSharedEvent(long eventTime, String driverId, String rideId,
                                           String cityId, double pickupLat, double pickupLng,
                                           int estimatedDurationMinutes, double estimatedFare,
                                           String cancellationReason, int passengerCount,
                                           double poolingScore) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.cancellationReason = cancellationReason;
        this.passengerCount     = passengerCount;
        this.poolingScore       = poolingScore;
    }

    public static DriverRideCancelledSharedEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCancelledSharedEvent(raw);
    }

    public String getCancellationReason() { return cancellationReason; }
    public int getPassengerCount()        { return passengerCount; }
    public double getPoolingScore()       { return poolingScore; }
}
