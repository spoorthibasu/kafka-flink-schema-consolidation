package com.example.consolidation.events;

import java.util.Map;

/** Emitted when a driver accepts a standard (single-passenger) ride request. */
public class DriverRideAcceptedStandardEvent extends BaseRideEvent {

    private DriverRideAcceptedStandardEvent(Map<String, Object> raw) {
        super(raw);
    }

    public DriverRideAcceptedStandardEvent(long eventTime, String driverId, String rideId,
                                            String cityId, double pickupLat, double pickupLng,
                                            int estimatedDurationMinutes, double estimatedFare) {
        super(eventTime, driverId, rideId, cityId, pickupLat, pickupLng,
              estimatedDurationMinutes, estimatedFare);
    }

    public static DriverRideAcceptedStandardEvent fromMap(Map<String, Object> raw) {
        return new DriverRideAcceptedStandardEvent(raw);
    }
}
