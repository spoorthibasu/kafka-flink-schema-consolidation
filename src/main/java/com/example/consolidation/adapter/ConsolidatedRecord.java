package com.example.consolidation.adapter;

import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/**
 * Java representation of the consolidated DriverRideActivityRecord Avro schema.
 *
 * This single class replaces what would otherwise be N separate event classes,
 * one per event-type/ride-type combination. The discriminator fields (eventType,
 * rideType) identify the variant, and nullable attribute blocks hold type-specific
 * data.
 *
 * Null fields in this class map to null fields in the Avro schema, which in turn
 * map to null values in the downstream data lake table. Consumers should always
 * check the discriminator fields before accessing type-specific fields.
 */
public class ConsolidatedRecord {

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
    private RideEventType eventType;
    private RideType rideType;

    // Completion-only fields: populated for RIDE_COMPLETED, null otherwise
    private Double fareAmount;
    private Integer durationMinutes;
    private Double distanceKm;

    // Cancellation-only fields: populated for RIDE_CANCELLED, null otherwise
    private String cancellationReason;

    // Type-specific attribute blocks: at most one is populated per record
    private SharedRideAttributes sharedRideAttributes;
    private ScheduledRideAttributes scheduledRideAttributes;

    // Getters and setters

    public long getEventTime() { return eventTime; }
    public void setEventTime(long eventTime) { this.eventTime = eventTime; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    public String getCityId() { return cityId; }
    public void setCityId(String cityId) { this.cityId = cityId; }

    public double getPickupLat() { return pickupLat; }
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }

    public double getPickupLng() { return pickupLng; }
    public void setPickupLng(double pickupLng) { this.pickupLng = pickupLng; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(double estimatedFare) { this.estimatedFare = estimatedFare; }

    public RideEventType getEventType() { return eventType; }
    public void setEventType(RideEventType eventType) { this.eventType = eventType; }

    public RideType getRideType() { return rideType; }
    public void setRideType(RideType rideType) { this.rideType = rideType; }

    public Double getFareAmount() { return fareAmount; }
    public void setFareAmount(Double fareAmount) { this.fareAmount = fareAmount; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public SharedRideAttributes getSharedRideAttributes() { return sharedRideAttributes; }
    public void setSharedRideAttributes(SharedRideAttributes sharedRideAttributes) { this.sharedRideAttributes = sharedRideAttributes; }

    public ScheduledRideAttributes getScheduledRideAttributes() { return scheduledRideAttributes; }
    public void setScheduledRideAttributes(ScheduledRideAttributes scheduledRideAttributes) { this.scheduledRideAttributes = scheduledRideAttributes; }
}
