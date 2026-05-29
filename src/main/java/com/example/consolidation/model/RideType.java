package com.example.consolidation.model;

/** The other discriminator field. Says which ride product an event belongs to. */
public enum RideType {

    STANDARD,   // standardRideAttributes is populated
    SHARED,     // sharedRideAttributes is populated
    SCHEDULED   // scheduledRideAttributes is populated
}
