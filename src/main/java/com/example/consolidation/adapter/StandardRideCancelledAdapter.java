package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideCancelledStandardEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideCancelledStandardEvent to the consolidated schema. */
public class StandardRideCancelledAdapter
        implements RecordAdapter<DriverRideCancelledStandardEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideCancelledStandardEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(RideEventType.RIDE_CANCELLED);
        record.setRideType(RideType.STANDARD);
        record.setCancellationReason(event.getCancellationReason());
        return record;
    }
}
