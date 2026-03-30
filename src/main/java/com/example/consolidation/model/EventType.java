package com.example.consolidation.model;

/**
 * Discriminator enum identifying the type of driver ride activity event.
 *
 * Used as a discriminator field in {@link DriverRideActivityRecord} to identify
 * what kind of event a record represents, without requiring consumers to query
 * different tables for different event types.
 */
public enum EventType {

    /**
     * The driver has accepted the ride request.
     * fareAmount, durationMinutes, distanceKm, and cancellationReason will be null.
     */
    ACCEPTED,

    /**
     * The driver has picked up the passenger and the ride has started.
     * fareAmount, durationMinutes, distanceKm, and cancellationReason will be null.
     */
    STARTED,

    /**
     * The ride has been completed.
     * fareAmount, durationMinutes, and distanceKm will be populated.
     * cancellationReason will be null.
     */
    COMPLETED,

    /**
     * The ride was cancelled before completion.
     * cancellationReason will be populated.
     * fareAmount, durationMinutes, and distanceKm will be null.
     */
    CANCELLED
}
