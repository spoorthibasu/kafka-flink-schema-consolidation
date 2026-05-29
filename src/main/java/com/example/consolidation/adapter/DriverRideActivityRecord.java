package com.example.consolidation.adapter;

import com.example.consolidation.model.EventType;
import com.example.consolidation.model.RideType;

/**
 * The one output record, matching DriverRideActivityRecord.avsc. Replaces the 12
 * per-variant classes: the discriminators say which variant, and only that
 * variant's attribute block is set.
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
    private Integer durationMins;
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

    public Integer getDurationMins()                             { return durationMins; }
    public void setDurationMins(Integer v)                       { this.durationMins = v; }

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

    /** Shows the discriminators and only the fields that are set, so print() output is readable. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DriverRideActivityRecord{")
                .append("eventType=").append(eventType)
                .append(", rideType=").append(rideType)
                .append(", driverId=").append(driverId)
                .append(", rideId=").append(rideId)
                .append(", cityId=").append(cityId);
        if (fareAmount != null)             sb.append(", fareAmount=").append(fareAmount);
        if (durationMins != null)           sb.append(", durationMins=").append(durationMins);
        if (distanceKm != null)             sb.append(", distanceKm=").append(distanceKm);
        if (cancellationReason != null)     sb.append(", cancellationReason=").append(cancellationReason);
        if (standardRideAttributes != null) sb.append(", standardRideAttributes=").append(standardRideAttributes);
        if (sharedRideAttributes != null)   sb.append(", sharedRideAttributes=").append(sharedRideAttributes);
        if (scheduledRideAttributes != null) sb.append(", scheduledRideAttributes=").append(scheduledRideAttributes);
        return sb.append('}').toString();
    }
}
