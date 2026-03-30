package com.example.consolidation.adapter;

import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/**
 * Java representation of the consolidated DriverRideActivityRecord Avro schema.
 *
 * This single class replaces what would otherwise be N separate event classes,
 * one per event-type/ride-type combination. The discriminator fields (eventType,
 * rideType) identify the variant, and nullable attribute blocks hold type-specific
 * data.
 */
public class DriverRideActivityRecord {

    // Shared fields: always populated
    private long eventTime;
    private String driverId;
    private String rideId;
    private String cityId;
    private double pickupLat;
    private double pickupLng;
    private int estimatedDurationMinutes;
    private double estimatedFare;

    // Discriminator fields: always populated, identify the variant
    private EventType eventType;
    private RideType rideType;

    // Completion-only fields: populated for COMPLETED, null otherwise
    private Double fareAmount;
    private Integer durationMinutes;
    private Double distanceKm;

    // Cancellation-only fields: populated for CANCELLED, null otherwise
    private String cancellationReason;

    // Type-specific attribute blocks: at most one is populated per record
    private StandardRideAttributes standardRideAttributes;
    private SharedRideAttributes sharedRideAttributes;
    private ScheduledRideAttributes scheduledRideAttributes;

    public long getEventTime()                                      { return eventTime; }
    public void setEventTime(long v)                                { this.eventTime = v; }

    public String getDriverId()                                     { return driverId; }
    public void setDriverId(String v)                               { this.driverId = v; }

    public String getRideId()                                       { return rideId; }
    public void setRideId(String v)                                 { this.rideId = v; }

    public String getCityId()                                       { return cityId; }
    public void setCityId(String v)                                 { this.cityId = v; }

    public double getPickupLat()                                    { return pickupLat; }
    public void setPickupLat(double v)                              { this.pickupLat = v; }

    public double getPickupLng()                                    { return pickupLng; }
    public void setPickupLng(double v)                              { this.pickupLng = v; }

    public int getEstimatedDurationMinutes()                        { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int v)                  { this.estimatedDurationMinutes = v; }

    public double getEstimatedFare()                                { return estimatedFare; }
    public void setEstimatedFare(double v)                          { this.estimatedFare = v; }

    public EventType getEventType()                                 { return eventType; }
    public void setEventType(EventType v)                           { this.eventType = v; }

    public RideType getRideType()                                   { return rideType; }
    public void setRideType(RideType v)                             { this.rideType = v; }

    public Double getFareAmount()                                   { return fareAmount; }
    public void setFareAmount(Double v)                             { this.fareAmount = v; }

    public Integer getDurationMinutes()                             { return durationMinutes; }
    public void setDurationMinutes(Integer v)                       { this.durationMinutes = v; }

    public Double getDistanceKm()                                   { return distanceKm; }
    public void setDistanceKm(Double v)                             { this.distanceKm = v; }

    public String getCancellationReason()                           { return cancellationReason; }
    public void setCancellationReason(String v)                     { this.cancellationReason = v; }

    public StandardRideAttributes getStandardRideAttributes()       { return standardRideAttributes; }
    public void setStandardRideAttributes(StandardRideAttributes v) { this.standardRideAttributes = v; }

    public SharedRideAttributes getSharedRideAttributes()           { return sharedRideAttributes; }
    public void setSharedRideAttributes(SharedRideAttributes v)     { this.sharedRideAttributes = v; }

    public ScheduledRideAttributes getScheduledRideAttributes()     { return scheduledRideAttributes; }
    public void setScheduledRideAttributes(ScheduledRideAttributes v) { this.scheduledRideAttributes = v; }
}
