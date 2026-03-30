package com.example.consolidation.events;

import java.util.Map;

/**
 * Fields shared across all ride event types and ride variants.
 *
 * In a production Kafka pipeline these fields are present in every fragmented
 * schema, duplicated across all 12 schema definitions. The 80-95% structural
 * overlap between those schemas is what motivates consolidation.
 */
public abstract class BaseRideEvent {

    private final long eventTime;
    private final String driverId;
    private final String rideId;
    private final String cityId;
    private final double pickupLat;
    private final double pickupLng;
    private final int estimatedDurationMinutes;
    private final double estimatedFare;

    protected BaseRideEvent(Map<String, Object> raw) {
        this.eventTime               = (Long)    raw.get("eventTime");
        this.driverId                = (String)  raw.get("driverId");
        this.rideId                  = (String)  raw.get("rideId");
        this.cityId                  = (String)  raw.get("cityId");
        this.pickupLat               = (Double)  raw.get("pickupLat");
        this.pickupLng               = (Double)  raw.get("pickupLng");
        this.estimatedDurationMinutes = (Integer) raw.get("estimatedDurationMinutes");
        this.estimatedFare           = (Double)  raw.get("estimatedFare");
    }

    protected BaseRideEvent(long eventTime, String driverId, String rideId, String cityId,
                             double pickupLat, double pickupLng,
                             int estimatedDurationMinutes, double estimatedFare) {
        this.eventTime                = eventTime;
        this.driverId                 = driverId;
        this.rideId                   = rideId;
        this.cityId                   = cityId;
        this.pickupLat                = pickupLat;
        this.pickupLng                = pickupLng;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.estimatedFare            = estimatedFare;
    }

    public long getEventTime()               { return eventTime; }
    public String getDriverId()              { return driverId; }
    public String getRideId()                { return rideId; }
    public String getCityId()                { return cityId; }
    public double getPickupLat()             { return pickupLat; }
    public double getPickupLng()             { return pickupLng; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public double getEstimatedFare()         { return estimatedFare; }
}
