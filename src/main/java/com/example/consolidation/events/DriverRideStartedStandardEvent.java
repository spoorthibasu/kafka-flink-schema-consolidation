package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver picks up the passenger and starts a standard ride. */
public class DriverRideStartedStandardEvent extends BaseRideEvent {

    private DriverRideStartedStandardEvent(Map<String, Object> raw) {
        super(raw);
    }

    public DriverRideStartedStandardEvent(long eventTime, String driverId, String rideId,
                                           String cityId, double pickupLat, double pickupLng,
                                           int estimatedDurationMinutes, double estimatedFare) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
    }

    public static DriverRideStartedStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideStartedStandardEvent(raw);
    }
}
