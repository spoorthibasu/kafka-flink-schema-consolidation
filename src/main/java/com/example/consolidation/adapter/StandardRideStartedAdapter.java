package com.example.consolidation.adapter;

import com.example.consolidation.events.DriverRideStartedStandardEvent;
import com.example.consolidation.model.RideEventType;
import com.example.consolidation.model.RideType;

/** Maps a DriverRideStartedStandardEvent to the consolidated schema. */
public class StandardRideStartedAdapter
        implements RecordAdapter<DriverRideStartedStandardEvent, ConsolidatedRecord> {

    @Override
    public ConsolidatedRecord adapt(String orgId, DriverRideStartedStandardEvent event) {
        ConsolidatedRecord record = new ConsolidatedRecord();
        record.setEventTime(event.getEventTime());
        record.setDriverId(event.getDriverId());
        record.setRideId(event.getRideId());
        record.setCityId(event.getCityId());
        record.setPickupLat(event.getPickupLat());
        record.setPickupLng(event.getPickupLng());
        record.setEstimatedDurationMinutes(event.getEstimatedDurationMinutes());
        record.setEstimatedFare(event.getEstimatedFare());
        record.setEventType(RideEventType.RIDE_STARTED);
        record.setRideType(RideType.STANDARD);
        return record;
    }
}
