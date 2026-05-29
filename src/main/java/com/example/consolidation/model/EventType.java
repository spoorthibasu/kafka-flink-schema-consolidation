package com.example.consolidation.model;

/** One of the two discriminator fields. Says where in the ride lifecycle an event sits. */
public enum EventType {

    ACCEPTED,
    STARTED,
    COMPLETED,   // fareAmount, durationMins, distanceKm are populated
    CANCELLED    // cancellationReason is populated
}
