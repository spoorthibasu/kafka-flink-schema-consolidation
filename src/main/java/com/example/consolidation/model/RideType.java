package com.example.consolidation.model;

/**
 * Discriminator enum identifying the ride product type.
 *
 * Used alongside {@link RideEventType} in {@link DriverRideActivityRecord} to fully
 * identify the variant of each record. Together these two discriminators replace the
 * need for separate schemas and tables per event-type/ride-type combination.
 */
public enum RideType {

    /**
     * A standard single-passenger ride.
     * sharedRideAttributes and scheduledRideAttributes will be null.
     */
    STANDARD,

    /**
     * A shared (pooled) ride where multiple passengers may share the vehicle.
     * sharedRideAttributes will be populated.
     * scheduledRideAttributes will be null.
     */
    SHARED,

    /**
     * A pre-scheduled ride booked in advance.
     * scheduledRideAttributes will be populated.
     * sharedRideAttributes will be null.
     */
    SCHEDULED
}
