package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideAcceptedStandardEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/**
 * Maps a DriverRideAcceptedStandardEvent to the consolidated schema.
 *
 * No Flink dependency. Pure transformation logic that can be unit tested
 * without any framework setup.
 */
public class StandardRideAcceptedAdapter
        implements RecordAdapter<DriverRideAcceptedStandardEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideAcceptedStandardEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(RideEventType.RIDE_ACCEPTED);
        record.setRideType(RideType.STANDARD);
        // No ride-type-specific attribute block for standard rides.
        // fareAmount, durationMinutes, distanceKm, cancellationReason remain null.
        return record;
    }
}
