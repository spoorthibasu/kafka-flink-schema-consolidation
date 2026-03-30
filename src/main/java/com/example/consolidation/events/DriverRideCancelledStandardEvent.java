package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a standard ride is cancelled before completion. */
public class DriverRideCancelledStandardEvent extends BaseRideEvent {

    private final String cancellationReason;

    private DriverRideCancelledStandardEvent(Map<String, Object> raw) {
        super(raw);
        this.cancellationReason = (String) raw.get("cancellationReason");
    }

    public DriverRideCancelledStandardEvent(long eventTime, String driverId, String rideId,
                                             String cityId, double pickupLat, double pickupLng,
                                             int estimatedDurationMinutes, double estimatedFare,
                                             String cancellationReason) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
        this.cancellationReason = cancellationReason;
    }

    public static DriverRideCancelledStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideCancelledStandardEvent(raw);
    }

    public String getCancellationReason() { return cancellationReason; }
}
